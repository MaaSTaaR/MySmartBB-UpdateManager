/*
 * Copyright (C) 2011, 2012, IBM Corporation and others.
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// This file derived from jGit's org.eclipse.jgit.api.AddCommand.

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.util.FileUtils;
import org.eclipse.jgit.util.IO;

public class MySmartApply 
{

	private InputStream in;
	private String local_path;
	private List<File> failed_files;
	private ApplyPatchListener listener = null;
	
	MySmartApply( String local_path, ApplyPatchListener listener )
	{
		this( local_path );
		
		this.listener = listener;
	}
	
	MySmartApply( String local_path )
	{
		this.local_path = local_path;
		this.failed_files = new ArrayList<File>();
	}
	
	/**
	 * @param in
	 *            the patch to apply
	 * @return this instance
	 */
	public MySmartApply setPatch(InputStream in) {
		this.in = in;
		
		return this;
	}

	/**
	 * Executes the {@code ApplyCommand} command with all the options and
	 * parameters collected by the setter methods (e.g.
	 * {@link #setPatch(InputStream)} of this class. Each instance of this class
	 * should only be used for one invocation of the command. Don't call this
	 * method twice on an instance.
	 *
	 * @return an {@link ApplyResult} object representing the command result
	 * @throws GitAPIException
	 * @throws PatchFormatException
	 * @throws PatchApplyException
	 */
	public ApplyResult call() throws GitAPIException, PatchFormatException,
			PatchApplyException {
		ApplyResult r = new ApplyResult();
		try {
			final Patch p = new Patch();
			try {
				p.parse(in);
			} finally {
				in.close();
			}
			if (!p.getErrors().isEmpty())
				throw new PatchFormatException(p.getErrors());
			for (FileHeader fh : p.getFiles()) {
				
				if ( listener != null ) listener.setFilename( getFile( ( fh.getChangeType() != ChangeType.ADD ) ? fh.getOldPath() : fh.getNewPath(), false ).getName() );
				
				if ( fh.getChangeType() == ChangeType.ADD || fh.getChangeType() == ChangeType.MODIFY || fh.getChangeType() == ChangeType.COPY )
				{
					if ( fh.getNewPath().contains( ".lang.php" ) ||
							fh.getNewPath().contains( ".gitignore" ) ||
							fh.getNewPath().equals( "includes/config.php" ) ||
							fh.getNewPath().contains( "/images" ) )
					{
						if ( fh.getNewPath().contains( ".lang.php" ) || fh.getNewPath().contains( "/images" ) )
							this.failed_files.add( getFile( ( fh.getChangeType() != ChangeType.ADD ) ? fh.getOldPath() : fh.getNewPath() , false ) );
						
						if ( listener != null ) listener.skipped();
						continue;
					}
				}
				
				if ( listener != null ) listener.started();
				
				ChangeType type = fh.getChangeType();
				File f = null;
				switch (type) {
				case ADD:
					f = getFile(fh.getNewPath(), true);
					apply(f, fh);
					break;
				case MODIFY:
					f = getFile(fh.getOldPath(), false);
					apply(f, fh);
					break;
				case DELETE:
					f = getFile(fh.getOldPath(), false);
					if (!f.delete())
						throw new PatchApplyException(MessageFormat.format(
								JGitText.get().cannotDeleteFile, f));
					break;
				case RENAME:
					f = getFile(fh.getOldPath(), false);
					File dest = getFile(fh.getNewPath(), false);
					if (!f.renameTo(dest))
						throw new PatchApplyException(MessageFormat.format(
								JGitText.get().renameFileFailed, f, dest));
					break;
				case COPY:
					f = getFile(fh.getOldPath(), false);
					byte[] bs = IO.readFully(f);
					FileOutputStream fos = new FileOutputStream(getFile(
							fh.getNewPath(),
							true));
					try {
						fos.write(bs);
					} finally {
						fos.close();
					}
				}
				
				// ... //
				
				if ( !failed_files.contains( f ) && type != ChangeType.DELETE )
					r.addUpdatedFile(f);
				
				// ... //
			}
		} catch (IOException e) {
			throw new PatchApplyException(MessageFormat.format(
					JGitText.get().patchApplyException, e.getMessage()), e);
		}
		
		return r;
	}

	private File getFile(String path, boolean create)
			throws PatchApplyException {
		File f = new File(this.local_path + path);
		if (create)
			try {
				File parent = f.getParentFile();
				FileUtils.mkdirs(parent, true);
				FileUtils.createNewFile(f);
			} catch (IOException e) {
				throw new PatchApplyException(MessageFormat.format(
						JGitText.get().createNewFileFailed, f), e);
			}
		return f;
	}

	/**
	 * @param f
	 * @param fh
	 * @throws IOException
	 * @throws PatchApplyException
	 */
	private void apply(File f, FileHeader fh)
			throws IOException, PatchApplyException {
		
		if ( failed_files.contains( f ) )
			return;
		
		RawText rt = new RawText(f);
		
		List<String> oldLines = new ArrayList<String>(rt.size());
		
		for (int i = 0; i < rt.size(); i++)
			oldLines.add(rt.getString(i));
		
		List<String> newLines = new ArrayList<String>(oldLines);
		
		for (HunkHeader hh : fh.getHunks()) 
		{
			StringBuilder hunk = new StringBuilder();
			
			for (int j = hh.getStartOffset(); j < hh.getEndOffset(); j++)
				hunk.append((char) hh.getBuffer()[j]);
			
			RawText hrt = new RawText(hunk.toString().getBytes());
			
			List<String> hunkLines = new ArrayList<String>(hrt.size());
			
			for (int i = 0; i < hrt.size(); i++)
			{
				String line = hrt.getString(i);

				line = removeInvalidChars( line );
				
				hunkLines.add( line );
			}
						
			int pos = 0;
			for (int j = 1; j < hunkLines.size(); j++) {
				
				String hunkLine = hunkLines.get(j);
				
				// We need the comparison only in case of removing a line or do nothing with it.
				String newLine = null;
				if ( hunkLine.charAt(0) == ' ' || hunkLine.charAt(0) == '-' )
					newLine = removeInvalidChars( newLines.get(hh.getNewStartLine() - 1 + pos) );
								
				switch (hunkLine.charAt(0)) {
				
				case ' ':
					if ( !newLine.equals( hunkLine.substring( 1 ) ) ) {						
						failed_files.add( f );
						if ( listener != null ) listener.failed();
						return;
					}
					pos++;
					break;
				case '-':
					if (!newLine.equals(hunkLine.substring(1))) {
						failed_files.add( f );
						if ( listener != null ) listener.failed();
						return;
					}
					newLines.remove(hh.getNewStartLine() - 1 + pos);
					break;
				case '+':
					newLines.add(hh.getNewStartLine() - 1 + pos,
							hunkLine.substring(1));
					pos++;
					break;
				}
			}
		}

		if (!isNoNewlineAtEndOfFile(fh))
			newLines.add(""); //$NON-NLS-1$
		if (!rt.isMissingNewlineAtEnd())
			oldLines.add(""); //$NON-NLS-1$
		if (!isChanged(oldLines, newLines))
			return; // don't touch the file
		StringBuilder sb = new StringBuilder();
		for (String l : newLines) {
			// don't bother handling line endings - if it was windows, the \r is
			// still there!
			sb.append(l).append('\n');
		}
		sb.deleteCharAt(sb.length() - 1);
		FileWriter fw = new FileWriter(f);
		fw.write(sb.toString());
		fw.close();
		
		if ( listener != null ) listener.done();
	}

	private static boolean isChanged(List<String> ol, List<String> nl) {
		if (ol.size() != nl.size())
			return true;
		for (int i = 0; i < ol.size(); i++)
			if (!ol.get(i).equals(nl.get(i)))
				return true;
		return false;
	}

	private boolean isNoNewlineAtEndOfFile(FileHeader fh) {
		
		// ... //
		
		if ( fh.getHunks().size() == 0 )
			return false;
		
		// ... //
		
		HunkHeader lastHunk = fh.getHunks().get(fh.getHunks().size() - 1);
		RawText lhrt = new RawText(lastHunk.getBuffer());
		return lhrt.getString(lhrt.size() - 1).equals(
				"\\ No newline at end of file"); //$NON-NLS-1$
	}
	
	private String removeInvalidChars( String txt )
	{
		StringBuilder builder = new StringBuilder();
		
		for ( int k = 0; k < txt.length(); k++ )
		{
			char c = txt.charAt( k );
			
			// Removes Byte order mark
			if ( 	c != '\uFFEF' && c != '\uFFBB' && c != '\uFFBF' && 
					c != '\uFEFF' && c != '\uBBFF' && c != '\uBFFF' &&
					( (int) c != 0xd ) )
			{
				builder.append( c );
			}
		}
		
		return builder.toString();
	}
	
	public List<File> getFailedFiles()
	{
		return this.failed_files;
	}
}