// This file is part of Appman.
//
// TO DO: better copyright notice. GPL?
//

package appman;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.net.ConnectException;
import java.rmi.RemoteException;


import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import appman.task.Task;
import appman.task.TaskState;


/**
 * Implements a constrained environment for tasks manipulating their input and output
 * files.
 *
 * <p>Ideally, the system should ensure the task could not read and write outside its
 * own  sandbox. This checking, although, is not yet implemented.
 *
 * <p>TO DO: implement access constraints
 *
 * @author last modified by $Author$
 * @version $Id$
 */
public class SandBox
{
    private static final String SYSPROP_TMPDIR  = "java.io.tmpdir";
    private static final String SYSPROP_USER    = "user.name";
    private static final String SLASH           = File.pathSeparator;
    private static final String DOT             = File.separator;
    private static final String SANDBOX_DIR_NAME= "appman-sandbox";
    private static final int    DFLT_CACHE_TBL  = 200;
    private static final int    DFLT_SB_TBL     = 400;

        /**
         * Under which local diretory, sandbox data will be stored. For better
         * performance, it should be placed in a local (not network) filesystem.
         */
    private static final File SANDBOX_ROOT;
    
        /**
         * A cache manager to reduce download time of files defined as task's
         * dependecies.
         */
    private static CacheManager cacheManager = new CacheManager();

        /**
         * Parent dir of files hosted in this sandbox.
         */
    private File sandboxDir;

        /**
         * File inside the sandbox to which command execution output will be redirected.
         */
    private File cmdOutput;

        /**
         * ID of the task to which this sand-box is bound.
         */
    private String taskId;
    
        //
        // STATIC INITIALIZER : detect the best location for the sandbox
        //
    static {
        File tmp = null;
        File root = null;
        try {
                // by default, place it under the default "tmp" directory with a username
                // suffix
            root = new File(System.getProperty(SYSPROP_TMPDIR)+SLASH
                            +SANDBOX_DIR_NAME+DOT+System.getProperty(SYSPROP_USER));
                // ensure it exists and is writable
            if ( root.exists() || root.mkdirs() ) {
                tmp = File.createTempFile("appman","appman", root);
            }
            else root = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            root = null;
        }
        finally {
                // fallback to the current working diretory
            if ( root == null ) {
                root = new File(SANDBOX_DIR_NAME);
                root.mkdir();
            }
            if ( tmp != null ) tmp.delete();
        }

        __debug__("sandbox root="+root.getAbsolutePath());
        
        SANDBOX_ROOT=root;
    }

        //////////////////////////////////////////////////////////////
        // PUBLIC METHODS
        //////////////////////////////////////////////////////////////
    
        /**
         * Factory method, creates a new sand-box instance for the task described by
         * <code>TaskName</code>
         *
         * @param tid the ID of the task to which the created sandbox will be bound
         * @return the corresponding sandbox object
         * @exception IOException if it fails to create the sandbox dir
         */
    public static SandBox newInstance(String tid) throws IOException
        {
            String dir = "task-"+Integer.toOctalString(
                AppManUtil.getExecutor().currentApplication().hashCode()
                + tid.hashCode());

            return new SandBox(tid, dir);
        }
    
        /**
         * Installs the given data file into this sandbox by creating a local copy of
         * the it.
         *
         * @param df the file to be locally installed
         * @exception IOException if an error occurs
         */
    public final void installFile(DataFile df) throws IOException
        {
            cacheManager.install(df, this.sandboxDir);
        }
    
        /**
         * Runs the specified command under this sandbox, returning the command's exit
         * code.
         *
         * @param command a command as it would be passed to Runtime.exec()
         * @return the command exit code.
         * @exception InterruptedException if the command execution was aborted
         * @exception IOException if an IO error occurs while calling Runtime.exec()
         */
    public synchronized int execute(String command)
        throws InterruptedException, IOException
        {
            String wd = sandboxDir.getAbsolutePath();
            String ioredir = " < /dev/null 2>&1 > "+cmdOutput.getAbsolutePath();

                // sanity check: the shell must be available
            File bash = new File("/bin/bash");
            if ( bash.exists() && bash.canRead() ) {
                    // if it is not, abort
                throw new java.io.FileNotFoundException("shell '/bin/bash' not found");
            }

                // everything seems ok, proceed with command execution
            String[] cmd = {
                "/bin/bash", "--login", "-c",
                "(cd "+wd+" && ("+command+"))"+ioredir};
            Process proc = Runtime.getRuntime().exec(cmd);
            try {
                return proc.waitFor();
            }
            catch (InterruptedException ie) {
                    // this thread has been interrupted, possibly the application is
                    // exiting due to an error
                __debug__("aborting execution due to interrupt");
                    // so do a local clean up 
                proc.destroy();
                    // FIX ME: should we already call cleanUp() here?
//                cleanUp();
                    // and propagate the exception
                throw (InterruptedException) ie.fillInStackTrace();
            }   
        }
    
        /**
         * Returns the output generated by the last call to <code>execute(String)</code>.
         *
         * @return a <code>String</code> value
         */
    public synchronized String getErrorMessage() throws IOException
        {
                //
                // XXX: experimental stuff, using the NIO API
                //
            if ( cmdOutput.exists() ) {
                FileChannel in = new FileInputStream(cmdOutput).getChannel();
                ByteBuffer buff = ByteBuffer.allocate((int) in.size());

                    // unlikely to happen, but lets check for a possible buffer overflow
                if (in.size() >= Integer.MAX_VALUE) {
                    __debug__("Warning: process error message has been truncated because "
                              +"contents of file "+cmdOutput+" exceeded the maximum buffer size.");
                }
                
                while ( in.read(buff) != -1 );

                    //
                    // FIX ME: how the transformation bellow deals with character encoding?
                    //
//                 buff.flip();
//                 return buff.asCharBuffer().toString();

                return new String(buff.array());
            }

            return "no error message";
        }
    
        /**
         * Removes all the files installed or created inside this sandbox.
         *
         */
    public void cleanUp()
        {
            if (! deltree(this.sandboxDir)) {
                __debug__("clean up failed for dir "+sandboxDir);
            }
        }


        //////////////////////////////////////////////////////////////
        // PRIVATE & PROTECTED METHODS
        //////////////////////////////////////////////////////////////
    
    private SandBox(String taskId, String dir) throws IOException
        {
            this.sandboxDir = new File(SANDBOX_ROOT, dir);

            if ( ! sandboxDir.mkdir() ) {
                throw new IOException ("Failed to create task sandbox directory at "
                                       +sandboxDir.getAbsolutePath());
            }
            else {
                this.cmdOutput = new File(sandboxDir,
                                              ".out."+sandboxDir.getName());
            }
        }
    
        /**
         * Recursivelly erases a directory subtree.
         *
         * @param dir a directory
         * @return a <code>boolean</code> value
         */
    private final boolean deltree(File dir)
        {
                // empty dir
            File[] l = dir.listFiles();
            for ( int i=0; i<l.length; i++ ) {
                if ( (l[i].isDirectory() && deltree(l[i])) || l[i].delete() ) continue;
                else return false;
            }
                // delete the directory itself
            return dir.delete();
        }

        /**
         * Facility for logging of debugging messages.
         *
         * @param msg a <code>String</code> value
         */
    private static final void __debug__(String msg)
        {
            Debug.debug("[SANDBOX] "+ msg, true);
        }
    
    @Override
	protected void finalize()
        {
            cleanUp();
        }

        //////////////////////////////////////////////////////////
        // INNER CLASSES
        //////////////////////////////////////////////////////////

        /**
         * Helper class. Implements a cache of downloaded files and optimizes the moving
         * of files between sand-boxes of different tasks (previously referred as
         * ImproveDownload).
         */
    private static class CacheManager
    {   
            /**
             * Mapping of DataFile objects to paths in the local filesystem.
             */
        private final Hashtable cachedFiles = new Hashtable(DFLT_CACHE_TBL);

        
        public synchronized void install(DataFile file, File sandboxDir)
            throws ConnectException, IOException
            {
                    // avoid downloading the file again if it's already in the cache
                File localpath = (File) cachedFiles.get(file);
                if ( localpath != null ) {
                        // instead, do a local (cheaper) copy 
                    copyLocalFile(localpath, sandboxDir);
                }
                else { // the file is not in the cache, so check its source
                    Task owner = file.getFromTask();

                    localpath = ( owner == null ) 
                            // external file, download it from the url given as its name
                        ? downloadFromURL(file.getName(), sandboxDir)
                            // internal (intermediate) file, get it from the remote file service
                        : downloadFromTask(owner, file.getName(), sandboxDir);

                        // update the cache table
                    cachedFiles.put(file, localpath);
                }
            }

            /**
             * Fetches the file from the specified URL and installs a copy of it into the
             * given sandbox directory.
             *
             * @param src src file as an URL
             * @param dstDir destination directory
             * @return the path where the file copy has been stored
             */
        private final File downloadFromURL(String src, File dstDir)
            throws java.net.MalformedURLException, IOException 
            {
                URL url = new URL(src);
                URLConnection conn = (new URL(src)).openConnection();
				//conn.setRequestProperty("Cache-Control:","max-age=0,
				// no-cache");
				//conn.setRequestProperty("Pragma:","no-cache");
				conn.connect();
                BufferedInputStream in = new BufferedInputStream(
                    conn.getInputStream());

                File dst = new File(dstDir, (new File(url.getPath()).getName()));                
                BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(dst));

                byte[] buff = new byte[4096];
                int n = 0;
                while ((n=in.read(buff)) != -1) out.write(buff, 0, n);
                
                in.close();
                out.close();

                return dst;
            }
        
            /**
             * Fetches the file from a remote task and installs a copy of it into the
             * given sandbox directory.
             *
             * @param remote_task the Task that produced the file
             * @param filename the name of the source file
             * @param dstDir destination directory
             * @return the path where the file copy has been stored
             */
        private final File downloadFromTask(Task remote_task, String filename, File dstDir)
            throws IOException, RemoteException, ConnectException
            {
                byte[] buffer = null;
                
                    // se a tarefa for estrangeira (de outro grafo) ent�o baixe o arquivo
                    // usando a refer�ncia remota do servi�o de arquivos do grid task
                if (remote_task.getState().getCode() == TaskState.TASK_FOREIGN_FINAL) {
                        // esta refer�ncia remota foi atualizada pelo
                        // submission manager <-- application manager
                        // <-- task <-- grid task
                    String contact_address_remote = remote_task
                        .getSubmissionManagerContactAddress();
                    
                    SubmissionManagerRemote smr = (SubmissionManagerRemote)
                        GeneralObjectActivator.getRemoteObjectReference(
                            contact_address_remote,
                            SubmissionManagerRemote.class);

                    __debug__("going to get remote file service from a foreign submission "
                              +"manager that owns the task ["+ remote_task.getTaskId()+ "]: "
                              + smr);
                    
                    buffer = smr.downloadFileFromGridTask(
                        remote_task.getTaskId(), filename);
                }
                else { // sen�o baixe o arquivo de forma convencional
                    GridFileServiceRemote gfs = remote_task.getRemoteGridTaskFileService();
                    if ( gfs == null ) {
                        throw new RemoteException("Remote grid file service not found!");
                    }
                    else buffer = gfs.downloadFile(filename);
                }

                File target = new File(dstDir, filename);
                
                createLocalFile(target, buffer);
                
                __debug__("remote file ["+ filename+"] installed at "+target);

                return target;
            }
        
            /**
             * Creates a file at the specified path in the local filesystem, filling it
             * withthe data provided in the byte buffer <code>contents</code>.
             *
             * @param path target localtion in the local file-system
             * @param contents data used to initialize the file
             */
        private final void createLocalFile(File path, byte[] contents) throws IOException
            {
                    //
                    // XXX: experimental stuff: using the NIO API
                    //
                ByteBuffer buff = ByteBuffer.wrap(contents);

                FileOutputStream out = new FileOutputStream(path);
                FileChannel ch = out.getChannel();

                do  ch.write(buff);  while (buff.hasRemaining());

                ch.close();
            }

            /**
             * Direct copy between local sand-boxes.
             *
             * <p>The filename is preserved.
             *
             * @param src source file
             * @param dstDir destination directory
             * @exception IOException if an error occurs
             */
        private final void copyLocalFile(File src, File dstDir)
            throws IOException
            {
                File dst = new File(dstDir, src.getName());
                    //
                    // XXX: experimental stuff: using the NIO API
                    //
                FileChannel sch = new FileInputStream(src).getChannel();
                FileChannel dch = new FileOutputStream(dst).getChannel();
                    // Copy file contents from source to destination
                dch.transferFrom(sch, 0, sch.size());

                sch.close();
                dch.close();
            }        
    }
}
