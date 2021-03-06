package client.ui;

import client.data.PrintingData;
import client.logic.Analyzer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static client.data.Constants.AREA_HEIGHT;
import static client.data.Constants.AREA_WIDTH;

/**
 * Created by vvrud on 14.11.16.
 *
 * @author VVRud
 */
class DrawArea extends JComponent {

    private Image image;
    private Graphics2D g2;
    private boolean mouseMoved;
    private boolean active = false;
    private boolean imageDrawn = false;

    private int beginX, beginY, endX, endY;
    private int currentX, currentY, oldX, oldY;

    private ArrayList<Integer> listX = new ArrayList<>();
    private ArrayList<Integer> listY = new ArrayList<>();

    private Analyzer analyzer = new Analyzer();

    DrawArea() {

        setDoubleBuffered(false);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    oldX = e.getX();
                    oldY = e.getY();

                    beginX = oldX;
                    beginY = oldY;

                    listX.add(beginX);
                    listY.add(beginY);

                    mouseMoved = false;
                    if (!active) active = true;
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    endX = e.getX();
                    endY = e.getY();

                    if (mouseMoved && ((beginX == endX) && (beginY == endY))) {
                        g2.drawOval(beginX, beginY, 1, 1);
                        System.out.printf("MOUSE MOVED. CIRCLE. (%d; %d)\n", beginX, beginY);
                    } else if (mouseMoved && ((beginX != endX) || (beginY != endY))) {
                        g2.drawOval(beginX, beginY, 1, 1);
                        g2.drawOval(endX, endY, 1, 1);
                        System.out.printf("MOUSE MOVED. CURVE. (%d; %d) -> (%d; %d)\n", beginX, beginY, endX, endY);
                    } else if (!mouseMoved) {
                        g2.drawOval(beginX, beginY, 1, 1);
                        System.out.printf("MOUSE WAS NOT MOVED. DOT. (%d; %d)\n", beginX, beginY);
                    } else System.out.println("Something went wrong!");

                    repaint();

                    analyzer.addToAnalyze(new ArrayList<>(listX), new ArrayList<>(listY));
                    System.out.println("X:" + listX.size() + " " + listX.toString());
                    System.out.println("Y:" + listY.size() + " " + listY.toString());

                    listX.clear();
                    listY.clear();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isEnabled()) {
                    currentX = e.getX();
                    currentY = e.getY();

                    listX.add(currentX);
                    listY.add(currentY);

                    if (g2 != null) {
                        g2.drawLine(oldX, oldY, currentX, currentY);
                        repaint();
                        oldX = currentX;
                        oldY = currentY;
                    }

                    mouseMoved = true;
                }
            }
        });
    }

    void drawChosenImage(BufferedImage img) {
        clear();
        imageDrawn = true;

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int width = 0;
        int height = 0;
        if (imgWidth > imgHeight) {
            width = AREA_WIDTH;
            height = (imgHeight * AREA_WIDTH) / imgWidth;
        } else if (imgHeight > imgWidth) {
            height = AREA_HEIGHT;
            width = (imgWidth * AREA_HEIGHT) / imgHeight;
        } else if (imgHeight == imgWidth) {
            width = imgWidth;
            height = imgHeight;
        }

        g2.drawImage(img, 0, 0, width, height, null);
        repaint();
        setEnabled(false);
    }

    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = createImage(getSize().width, getSize().height);
            g2 = (Graphics2D) image.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            clear();
        }
        g.drawImage(image, 0, 0, null);
    }

    void clear() {
        analyzer.clearLists();
        g2.setPaint(Color.white);
        g2.fillRect(0, 0, getSize().width, getSize().height);
        g2.setPaint(Color.black);
        repaint();
        setEnabled(true);
        if (imageDrawn) imageDrawn = false;
        if (active) active = false;
        if (PrintingData.getJpgFileCreated() != null) {
            PrintingData.setJpgFileCreated(null);
            WorkspaceWindow.getFileDir().setText("<file directory here>");
        }
    }

    Analyzer getAnalyzer() {
        return analyzer;
    }

    void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    boolean isActive() {
        return active;
    }

    boolean isImageDrawn() {
        return imageDrawn;
    }

    void graphicsToImage() {
        BufferedImage bufferedImage = (BufferedImage) image;
        try {
            File imgFile = File.createTempFile("jpg_rpp_", "jpg");
            ImageIO.write(bufferedImage, "jpg", imgFile);
            PrintingData.setJpgFileCreated(imgFile);
        } catch (IOException e) {
            System.out.println("Failed creating an Image!");
            e.printStackTrace();
        }
    }

    void bleachPicture() {
        BufferedImage bufferedImage = (BufferedImage) image;
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(bufferedImage.getRGB(x, y), true);
                int myGrey = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                Color myColor = new Color(myGrey, myGrey, myGrey, 0);
                bufferedImage.setRGB(x, y, myColor.getRGB());
            }
        }

        try {
            File imgFile = File.createTempFile("jpg_rpp_", "jpg");
            ImageIO.write(bufferedImage, "jpg", imgFile);
            PrintingData.setJpgFileCreated(imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        repaint();
    }
}
