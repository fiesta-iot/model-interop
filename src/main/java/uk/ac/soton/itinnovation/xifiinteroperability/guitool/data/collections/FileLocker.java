/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2017
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
// Created By : Nikolay Stanchev
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 3
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.xifiinteroperability.guitool.data.collections;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle file locking methods, intended to be used with the workspace of the tool,
 * that is to file lock everything in the workspace while the tool is active, so that only the tool can manipulate those files
 * 
 * @author ns17
 */
public class FileLocker {
    
    /**
     * locks a single file (not a directory!)
     * @param fileToLock the file object to lock
     * @return the FileLock object
     */
    public static FileLock lockFile(File fileToLock){
        // check that we are locking an existing file, if not return null
        if (!fileToLock.exists())
            return null;
        
        try {
            final RandomAccessFile ramf = new RandomAccessFile(fileToLock, "rws");
            return ramf.getChannel().tryLock();
        } 
        catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * appends a new lock to the currently defined locks
     * @param fileToLock the new file to lock
     * @param currentLocks the current list of locks
     */
    public static void addLock(File fileToLock, List<FileLock> currentLocks){
        if (currentLocks == null){
            return;
        } 
        
        final FileLock fLock = FileLocker.lockFile(fileToLock);
        if (fLock != null){
            currentLocks.add(fLock);
        }
    }
    
    /**
     * releases all the file locks in a given list
     * @param locks the list of locks
     * @return true if no exception was thrown (all locks released)
     */
    public static boolean releaseDirLocks(List<FileLock> locks){
        try {
            for (FileLock fLock : locks){
                fLock.release();
                fLock.channel().close();
            }

            return true;
        }
        catch (IOException ioe){
            return false;
        }
    } 
    
    /**
     * locks a whole directory including all of its content
     * @param dir the directory to lock (Path object)
     * @return the list of all file locks, that is the locks of all the content in the directory
     */
    public static List<FileLock> lockDir(Path dir){
        return FileLocker.lockDir(dir.toFile());
    }
    
    /**
     * locks a whole directory including all of its content
     * @param dir the directory to lock (File object)
     * @return the list of all file locks, that is the locks of all the content in the directory
     */
    public static List<FileLock> lockDir(File dir){
        return lockDir(dir, new ArrayList<>());
    }
    
    /**
     * locks a whole directory including all of its content
     * @param dir the directory to lock
     * @param currentLocks a list keeping track of the currently collected locks (the method uses recursion)
     * @return the list of all locks, that is the locks of all the content in the directory
     */
    private static List<FileLock> lockDir(File dir, List<FileLock> currentLocks){
        // check that file is an existing directory, if not return null
        if (!(dir.exists() && dir.isDirectory()))
            return null;
        
        // lock the content of the directory
        FileLock fLock;
        for (File f: dir.listFiles()){
            // if the file is a directory use recursion to get all locks of this folder
            if (f.isDirectory()){
                lockDir(f, currentLocks);
            }
            // else just get the lock of the single file and add it to the list of locks
            else {
                fLock = lockFile(f);
                if (fLock == null){
                    return null;
                }
                currentLocks.add(fLock);
            }
        }
        
        return currentLocks;
    }
    
}
