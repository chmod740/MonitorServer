package me.hupeng.java.monitorserver.main;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.mina.core.session.IoSession;

import me.hupeng.java.monitorserver.Mina.MinaUtil;
import me.hupeng.java.monitorserver.Mina.MyData;
import me.hupeng.java.monitorserver.Mina.SimpleListener;
import me.hupeng.java.monitorserver.util.OperateImage;

public class MonitorServer {
	public static MinaUtil minaUtil;
	
	public static byte[]  videoDatas[] = new byte[8][];
	
	
    /**
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new ImageViewerFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        
    }
}

class ImageViewerFrame extends JFrame{
	private JLabel label;
    private JFileChooser chooser;
    private static final int DEFAULT_WIDTH = OperateImage.width * 4;
    private static final int DEFAULT_HEIGHT = OperateImage.height * 2;
    
    
    public ImageViewerFrame(){
        setTitle("监控平台服务端");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        label = new JLabel();
        add(label);
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        JMenu menu = new JMenu("File");
        menubar.add(menu);
        JMenuItem exitItem = new JMenuItem("Close");
        menu.add(exitItem);
        
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        
        MonitorServer.minaUtil = MinaUtil.getInstance(new SimpleListener() {
			
			@Override
			public void onReceive(Object obj, IoSession ioSession) {
				MyData myData = (MyData)obj;
				//ImageIcon icon = new ImageIcon(myData.bitmap);
				//处理原始数据
				try{
					myData.bitmap = OperateImage.addString(myData.bitmap);
				}catch(Exception e){
					e.printStackTrace();
				}	
				MonitorServer.videoDatas[myData.clientId] = myData.bitmap;
				//label.setIcon(icon);
			}
		}, true, null);
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					draw(new DrawListener() {
						
						@Override
						public void draw(Icon icon) {
							label.setIcon(icon);
						}
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				
			}
		}).start();;
    }
    
    private void draw(DrawListener drawListener){
    	BufferedImage combined = new BufferedImage(OperateImage.width * 4, OperateImage.height * 2, BufferedImage.TYPE_INT_RGB);  
    	Graphics g = combined.getGraphics();  
    	for(int i = 0 ; i < 8 ; i++){
    		try {
    			if (MonitorServer.videoDatas[i] != null) {
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MonitorServer.videoDatas[i]);
					BufferedImage image = ImageIO.read(byteArrayInputStream);
    				g.drawImage(image,i % 4 * OperateImage.width, i /4 * OperateImage.height,null);
    			
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
    	}
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	try {
			ImageIO.write(combined,"jpg", byteArrayOutputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ImageIcon icon = new ImageIcon(byteArrayOutputStream.toByteArray());
    	drawListener.draw(icon);
    	try {
			byteArrayOutputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}