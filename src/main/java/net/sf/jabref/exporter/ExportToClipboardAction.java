/*  Copyright (C) 2003-2015 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.exporter;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import net.sf.jabref.gui.worker.AbstractWorker;
import net.sf.jabref.gui.BasePanel;
import net.sf.jabref.model.database.BibtexDatabase;
import net.sf.jabref.model.entry.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.gui.JabRefFrame;
import net.sf.jabref.logic.l10n.Localization;

/**
 * Created by IntelliJ IDEA.
 * User: alver
 * Date: Dec 12, 2006
 * Time: 6:22:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExportToClipboardAction extends AbstractWorker {

    private final JabRefFrame frame;
    private final BibtexDatabase database;

    /**
     * written by run() and read by update()
     */
    private String message;

    public ExportToClipboardAction(JabRefFrame frame, BibtexDatabase database) {
        this.frame = Objects.requireNonNull(frame);
        this.database = Objects.requireNonNull(database);
    }

    @Override
    public void run() {
        BasePanel panel = frame.getCurrentBasePanel();
        if (panel == null) {
            return;
        }
        if (panel.getSelectedEntries().length == 0) {
            message = Localization.lang("No entries selected.");
            getCallBack().update();
            return;
        }

        List<IExportFormat> exportFormats = new LinkedList<>(ExportFormats.getExportFormats().values());
        Collections.sort(exportFormats, (e1, e2) -> e1.getDisplayName().compareTo(e2.getDisplayName()));
        String[] exportFormatDisplayNames = new String[exportFormats.size()];
        for (int i = 0; i < exportFormats.size(); i++) {
            IExportFormat exportFormat = exportFormats.get(i);
            exportFormatDisplayNames[i] = exportFormat.getDisplayName();
        }

        JList<String> list = new JList<>(exportFormatDisplayNames);
        list.setBorder(BorderFactory.createEtchedBorder());
        list.setSelectionInterval(0, 0);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int answer = JOptionPane.showOptionDialog(frame, list, Localization.lang("Select export format"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                // @formatter:off
                new String[] {Localization.lang("Export with selected format"),
                        Localization.lang("Return to JabRef")},
                Localization.lang("Export with selected format"));
                // @formatter:on
        if (answer == JOptionPane.NO_OPTION) {
            return;
        }

        IExportFormat format = exportFormats.get(list.getSelectedIndex());

        // Set the global variable for this database's file directory before exporting,
        // so formatters can resolve linked files correctly.
        // (This is an ugly hack!)
        Globals.prefs.fileDirForDatabase = frame.getCurrentBasePanel().metaData()
                .getFileDirectory(Globals.FILE_FIELD);
        // Also store the database's file in a global variable:
        Globals.prefs.databaseFile = frame.getCurrentBasePanel().metaData().getFile();

        File tmp = null;
        Reader reader = null;
        try {
            // To simplify the exporter API we simply do a normal export to a temporary
            // file, and read the contents afterwards:
            tmp = File.createTempFile("jabrefCb", ".tmp");
            tmp.deleteOnExit();
            BibtexEntry[] bes = panel.getSelectedEntries();
            HashSet<String> entries = new HashSet<>(bes.length);
            for (BibtexEntry be : bes) {
                entries.add(be.getId());
            }

            // Write to file:
            format.performExport(database, panel.metaData(),
                    tmp.getPath(), panel.getEncoding(), entries);
            // Read the file and put the contents on the clipboard:
            StringBuilder sb = new StringBuilder();
            reader = new InputStreamReader(new FileInputStream(tmp), panel.getEncoding());
            int s;
            while ((s = reader.read()) != -1) {
                sb.append((char) s);
            }
            ClipboardOwner owner = (clipboard, content) -> {
                // Do nothing
            };
            //StringSelection ss = new StringSelection(sw.toString());
            RtfSelection rs = new RtfSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(rs, owner);
            message = Localization.lang("Entries exported to clipboard") + ": " + bes.length;

        } catch (Exception e) {
            e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
            message = Localization.lang("Error exporting to clipboard");
        } finally {
            // Clean up:
            if (tmp != null) {
                tmp.delete();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    @Override
    public void update() {
        frame.output(message);
    }

}
