package com.oshmidt;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;

/**
 * @author oshmidt
 *         <p>
 *         Class GistLocalRepository implementation. Created for store user data
 *         inside file system.
 */
public class GistLocalFileManager extends GistLocalRepository {

    private static final String CANT_FOUND = Messages
            .getString("com.oshmidt.gistManager.cantFound");
    private static final String IO_PROBLEM = Messages
            .getString("com.oshmidt.gistManager.IOProblem");
    private static final String WRONG_TYPE = Messages
            .getString("com.oshmidt.gistManager.wrongType");
    private static final String DOWNLOAD_PROBLEM = Messages
            .getString("com.oshmidt.gistManager.downloadProblem");
    private final int NUM = 24;
    
    private ObjectInputStream oin;

    /** Default path for local repository. */
    private static final String DEFAULT_PATH = "localRepository/";

    /** Extension for serialized gists. */
    private static final String GIST_FILE_EXT = ".gist";
    
    private File gistFolder;

    /**
     * Logger instance.
     */
    private static Logger logger = Logger.getLogger(GistLocalFileManager.class);

    /**
     * Loading default value for "repoPath". Set default value for
     * {@link com.oshmidt.GistLocalRepository#repoPath} from
     * {@link com.oshmidt.GistLocalFileManager#DEFAULT_PATH}.
     */
    public final void loadDefaultRepoPath() {
        setRepoPath(DEFAULT_PATH);
    }

    /**
     * GistRepository implementation. Deserialize gists from file system by
     * {@link com.oshmidt.GistLocalRepository#repoPath}
     *
     * @return Deserialized gists.
     */
    public final List<Gist> readGists() {
        ArrayList<Gist> gists = new ArrayList<Gist>();
        preparePath();
        gistFolder = new File(getRepoPath());
        if (!gistFolder.exists()) {
            return null;
        }
        FileFilter filter = createGistFilter();
        for (File gst : gistFolder.listFiles(filter)) {
            logger.info(gst.getName());
            gists.add(deserializeGist(gst));
        }
        return gists;
    }

    /**
     * @param gst
     *            - serialized gist file
     * @return - gist item
     */
    private Gist deserializeGist(final File gst) {
        FileInputStream fis;
        oin = null;
 //       Throwable throwable = null;
        Gist gist = null;
        try {
            fis = new FileInputStream(gst);
            oin = new ObjectInputStream(fis);
            gist = (Gist) oin.readObject();
            return gist;
        } catch (FileNotFoundException e) {
 //           throwable = e;
            logger.error(e + CANT_FOUND);
        } catch (IOException e) {
 //           throwable = e;
            logger.error(e + IO_PROBLEM);
        } catch (ClassNotFoundException e) {
 //           throwable = e;
            logger.error(e + WRONG_TYPE);
        }  finally {
//            if (throwable == null) {
//                oin.close();
//            }
//            else {
                try {
                   oin.close();
                } catch (Throwable unused) {
                    logger.error(unused);
//                }
            }
        }
        return gist;
    }

    /**
     * GistRepository implementation. Serialize gists into file system to path
     * {@link com.oshmidt.GistLocalRepository#repoPath}
     *
     * @param gists
     *            list.
     */
    public final void writeGists(final List<Gist> gists) {

        for (Gist gist : gists) {
            logger.info(gist.getId());
            serializeGist(gist);

        }
    }

    /**
     * @param gist
     *            - Gist item
     */
    private void serializeGist(final Gist gist) {
        try {
            FileOutputStream fos;
            safeMakeDir();
            fos = new FileOutputStream(getRepoPath() + gist.getId()
                    + GIST_FILE_EXT);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(gist);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            logger.error(e + IO_PROBLEM);
        }
    }

    /**
     * GistRepository implementation. Download Gist files to file system path
     * {@link com.oshmidt.GistLocalRepository#repoPath}
     *
     * @param gist
     *            - Gist item.
     */
    public final void writeFiles(final Gist gist) {
        Map<String, GistFile> gistFiles = gist.getFiles();
        Set<String> set = gistFiles.keySet();
        for (String s : set) {
            GistFile gf = gistFiles.get(s);
            message(gf.getFilename());
            downloadAndSaveGistFile(gf, gist);
        }
    }

    /**
     * @param gf
     *            - GistFile item
     * @param gist
     *            - Gist item
     */
    private void downloadAndSaveGistFile(final GistFile gf, final Gist gist) {
        URL website;
        try {
            website = new URL(gf.getRawUrl());
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            setRepoPath(getRepoPath() + File.separator + gist.getId()
                    + File.separator);
            safeMakeDir();
            FileOutputStream fos = new FileOutputStream(new File(getRepoPath(),
                    gf.getFilename()));
            fos.getChannel().transferFrom(rbc, 0, 1 << NUM);

            fos.close();
            loadDefaultRepoPath();
        } catch (MalformedURLException e) {
            logger.error(e + DOWNLOAD_PROBLEM);
        } catch (IOException e) {
            logger.error(e + IO_PROBLEM);
        }
    }

    /**
     * Download Gists files to file system path.
     * {@link com.oshmidt.GistLocalRepository#repoPath}
     *
     * @param gists
     *            list.
     */
    public final void writeFiles(final List<Gist> gists) {
        for (Gist gist : gists) {
            writeFiles(gist);
        }
    }

    /**
     * GistRepository implementation. NOT IMPLEMENTED !
     *
     * @return null
     */
    public final Map<Gist, List<GistFile>> readFiles() {
        return null;
    }

    /**
     * Method create and return file filter for serialized gists.
     *
     * @return FileFilter
     */
    private FileFilter createGistFilter() {
        FileFilter ff = new FileFilter() {
            public boolean accept(final File f) {
                return f.isFile() && f.getName().endsWith(GIST_FILE_EXT);
            }
        };
        return ff;
    }

    /**
     * Method create repository directory if it not exist.
     */
    private void safeMakeDir() {
        preparePath();
        File dir = new File(getRepoPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Method check availability repoPath field and set it to default if needed.
     */
    private void preparePath() {
        if (getRepoPath() == null) {
            loadDefaultRepoPath();
        }
    }

    /**
     * Transmit message to logger and console.
     *
     * @param mes
     *            - String message
     */
    private void message(final String mes) {
        logger.info(mes);
        System.out.println(mes);
    }
}
