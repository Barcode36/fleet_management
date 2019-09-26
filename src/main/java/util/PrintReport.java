package main.java.util;

import main.java.model.*;
import main.java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.io.InputStream;
import java.util.Map;

public class PrintReport extends JFrame {

    public void showReport(Map<String, Object> parameters, String resourceFile) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("main/resources/view/" +
                    resourceFile + ".jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
            JRViewer viewer = new JRViewer(print);
            viewer.setOpaque(true);
            viewer.setVisible(true);
            this.add(viewer);
            this.setSize(800, 800);
            this.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace();
        }
    }
}
