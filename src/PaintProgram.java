import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PaintProgram extends JPanel implements MouseMotionListener, ActionListener, MouseListener, KeyListener {
	
	JFrame frame;
	Stack<Object> points, undoRedoStack;
	ArrayList<Point> currentStroke;
	Color currentColor, backgroundColor, oldColor;
	
	JMenuBar bar;
	
	JMenu colorMenu;
	JMenuItem[] colorOptions;
	Color[] colors;
	
	JMenu widthMenu;
	JScrollBar widthBar;
	
	JButton freeLineButton, rectangleButton, ovalButton, undoButton, redoButton, eraserButton;
	ImageIcon freeLineImg, rectImg, ovalImg, loadImg, saveImg, undoImg, redoImg, eraserImg;
	boolean drawingLine = true, drawingRectangle = false, drawingOval = false, erasing = false;
	boolean firstClick = true;
	boolean shiftPressed;
	float currentPenWidth;
	int currentX, currentY, currentWidth, currentHeight;
	Shape currentShape;
	
	JMenu fileMenu;
	JMenuItem save, load, clear, exit;
	
	JFileChooser fileChooser;
	BufferedImage loadedImage;
	
	
	JColorChooser colorChooser;
	
	public PaintProgram () {
		frame = new JFrame("Paint Program");
		frame.add(this);
		
		bar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		
		save = new JMenuItem("Save", KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveImg = new ImageIcon("saveImg.png");
		saveImg = new ImageIcon(saveImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		
		load = new JMenuItem("Load", KeyEvent.VK_L);
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		loadImg = new ImageIcon("loadImg.png");
		loadImg = new ImageIcon(loadImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		
		clear = new JMenuItem("New");
		exit = new JMenuItem("Exit");		
		
		save.addActionListener(this);
		load.addActionListener(this);
		clear.addActionListener(this);
		exit.addActionListener(this);
		
		fileMenu.add(clear);
		fileMenu.add(load);
		fileMenu.add(save);
		fileMenu.add(exit);		
		
		String currDir = System.getProperty("user.dir");
		fileChooser = new JFileChooser(currDir);
		
		bar.add(fileMenu);
		
		colorMenu = new JMenu("Color Options");
		colorMenu.setLayout(new GridLayout(8, 1));
		
		colors = new Color[] {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA};
		
		colorOptions = new JMenuItem[colors.length];
		for (int i = 0; i < colorOptions.length; i++) {
			colorOptions[i] = new JMenuItem();
			colorOptions[i].putClientProperty("colorIndex", i);
			colorOptions[i].setBackground(colors[i]);
			colorOptions[i].addActionListener(this);
			colorOptions[i].setFocusable(false);
			colorMenu.add(colorOptions[i]);
		}
		currentColor = colors[0];
		colorChooser = new JColorChooser();
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener () {

			@Override
			public void stateChanged(ChangeEvent e) {
				currentColor = colorChooser.getColor();
			}
			
		});
		colorMenu.add(colorChooser);
		bar.add(colorMenu);
		
		freeLineImg = new ImageIcon("freeLineImg.png");
		freeLineImg = new ImageIcon(freeLineImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		rectImg = new ImageIcon("rectImg.png");
		rectImg = new ImageIcon(rectImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		ovalImg = new ImageIcon("ovalImg.png");
		ovalImg = new ImageIcon(ovalImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		
		freeLineButton = new JButton();
		freeLineButton.setIcon(freeLineImg);
		freeLineButton.setFocusPainted(false);
		freeLineButton.setBackground(Color.LIGHT_GRAY);
		freeLineButton.addActionListener(this);
		freeLineButton.setFocusable(false);
		
		rectangleButton = new JButton();
		rectangleButton.setIcon(rectImg);
		rectangleButton.setFocusPainted(false);
		rectangleButton.addActionListener(this);
		rectangleButton.setFocusable(false);
		
		ovalButton = new JButton();
		ovalButton.setIcon(ovalImg);
		ovalButton.setFocusPainted(false);
		ovalButton.addActionListener(this);
		ovalButton.setFocusable(false);
		
		eraserButton = new JButton();
		eraserImg = new ImageIcon("eraserImg.png");
		eraserImg = new ImageIcon(eraserImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		eraserButton.setIcon(eraserImg);
		eraserButton.setFocusPainted(false);
		eraserButton.addActionListener(this);
		eraserButton.setFocusable(false);
		
		undoButton = new JButton();
		undoImg = new ImageIcon("undoImg.png");
		undoImg = new ImageIcon(undoImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		undoButton.setIcon(undoImg);
		undoButton.setFocusPainted(false);
		undoButton.addActionListener(this);
		undoButton.setFocusable(false);
		
		redoButton = new JButton();
		redoImg = new ImageIcon("redoImg.png");
		redoImg = new ImageIcon(redoImg.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		redoButton.setIcon(redoImg);
		redoButton.setFocusPainted(false);
		redoButton.addActionListener(this);
		redoButton.setFocusable(false);
		
		bar.add(freeLineButton);
		bar.add(rectangleButton);
		bar.add(ovalButton);
		bar.add(eraserButton);
		bar.add(undoButton);
		bar.add(redoButton);
		
		widthMenu = new JMenu("Pen Width: 1");
		widthMenu.setLayout(new GridLayout(1, 2));
		widthBar = new JScrollBar(JScrollBar.HORIZONTAL, 1, 0, 1, 15);
		widthBar.setPreferredSize(new Dimension(200, widthBar.getPreferredSize().height));
		widthBar.setFocusable(false);
		widthBar.addAdjustmentListener(new AdjustmentListener () {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// TODO Auto-generated method stub
				currentPenWidth = widthBar.getValue();
				widthMenu.setText("Pen Width: " + widthBar.getValue());
			}
			
		});
		widthMenu.setText("Pen Width: " + widthBar.getValue());
		widthMenu.add(widthBar);
		bar.add(widthMenu);
		
		points = new Stack<Object>();
		undoRedoStack = new Stack<Object>();
		
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		
		shiftPressed = false;
		backgroundColor = Color.WHITE;
		oldColor = currentColor;
		
		currentStroke = new ArrayList<Point>();
		
		frame.addKeyListener(this);
		frame.setSize(1000, 700);
		frame.add(bar, BorderLayout.NORTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(backgroundColor);
		g2.fillRect(0,0,frame.getWidth(),frame.getHeight());
		
		if (loadedImage != null) {
			g2.drawImage(loadedImage, 0, 0, null);
		}
		
		//draw the points
		Iterator<Object> itr = points.iterator();
		while (itr.hasNext()) {
			Object stroke = itr.next();
			if (stroke instanceof Rectangle) {
				g2.setColor(((Rectangle) stroke).getColor());
				g2.setStroke(new BasicStroke(((Rectangle) stroke).getPenWidth()));
				g2.draw(((Rectangle) stroke).getShape());
			}
			else if (stroke instanceof Oval) {
				g2.setColor(((Oval) stroke).getColor());
				g2.setStroke(new BasicStroke(((Oval) stroke).getPenWidth()));
				g2.draw(((Oval) stroke).getShape());
			}
			else if (stroke instanceof ArrayList<?>) {
				for (int i = 0; i < ((ArrayList<Point>)stroke).size()-1; i++) {
					Point p1 = ((ArrayList<Point>)stroke).get(i);
					Point p2 = ((ArrayList<Point>)stroke).get(i+1);
					g2.setColor(p1.getColor());
					g2.setStroke(new BasicStroke(p1.getPenWidth()));
					g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
				}
			}
			
		}
		if (drawingLine) {
			for (int i = 0; i < currentStroke.size()-1; i++) {
				Point p1 = currentStroke.get(i);
				Point p2 = currentStroke.get(i+1);
				g2.setColor(p1.getColor());
				g2.setStroke(new BasicStroke(p1.getPenWidth()));
				g2.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
			}
		} else {
			if (currentShape != null) {
				g2.setColor(currentShape.getColor());
				g2.setStroke(new BasicStroke(currentShape.getPenWidth()));
				if (currentShape instanceof Rectangle) {
					g2.draw(((Rectangle) currentShape).getShape());
				}
				else if (currentShape instanceof Oval) {
					g2.draw(((Oval) currentShape).getShape());
				}
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == save) {
			save();
		}
		else if (e.getSource() == load) {
			load();
		}
		else if (e.getSource() == clear) {
			points = new Stack<Object>();
			loadedImage = null;
			repaint();
		}
		else if (e.getSource() == exit) {
			System.exit(0);
		} 
		else if (e.getSource() == freeLineButton) {
			drawingLine = true;
			drawingRectangle = false;
			drawingOval = false;
			erasing = false;
			freeLineButton.setBackground(Color.LIGHT_GRAY);
			rectangleButton.setBackground(null);
			ovalButton.setBackground(null);
			eraserButton.setBackground(null);
			currentColor = oldColor;
		}
		else if (e.getSource() == rectangleButton) {
			drawingLine = false;
			drawingRectangle = true;
			drawingOval = false;
			erasing = false;
			freeLineButton.setBackground(null);
			rectangleButton.setBackground(Color.LIGHT_GRAY);
			ovalButton.setBackground(null);
			eraserButton.setBackground(null);
			currentColor = oldColor;
		}
		else if (e.getSource() == ovalButton) {
			drawingLine = false;
			drawingRectangle = false;
			drawingOval = true;
			erasing = false;
			freeLineButton.setBackground(null);
			rectangleButton.setBackground(null);
			ovalButton.setBackground(Color.LIGHT_GRAY);
			eraserButton.setBackground(null);
			currentColor = oldColor;
		}
		else if (e.getSource() == eraserButton) {
			drawingLine = false;
			drawingRectangle = false;
			drawingOval = false;
			erasing = true;
			freeLineButton.setBackground(null);
			rectangleButton.setBackground(null);
			ovalButton.setBackground(null);
			eraserButton.setBackground(Color.LIGHT_GRAY);
			oldColor = currentColor;
			currentColor = backgroundColor;
		}
		else if (e.getSource() == undoButton) {
			undo();
		}
		else if (e.getSource() == redoButton) {
			redo();
		} else {
			if (erasing) {
				drawingLine = true;
				drawingRectangle = false;
				drawingOval = false;
				erasing = false;
				freeLineButton.setBackground(Color.LIGHT_GRAY);
				rectangleButton.setBackground(null);
				ovalButton.setBackground(null);
				eraserButton.setBackground(null);
			}
			int index = (int)((JMenuItem) e.getSource()).getClientProperty("colorIndex");
			currentColor = colors[index];
		}
		
	}
	
	public void undo () {
		if (!points.isEmpty()) {
			undoRedoStack.push(points.pop());
			repaint();
		}
		System.out.println("Undo: " + points.size() + " " + undoRedoStack.size());
	}
	
	public void redo () {
		if (!undoRedoStack.isEmpty()) {
			points.push(undoRedoStack.pop());
			repaint();
		}
		System.out.println("Redo: " + points.size() + " " + undoRedoStack.size());		
	}
	
	public void save () {
		FileFilter filter = new FileNameExtensionFilter("*.png", "png");
		fileChooser.setFileFilter(filter);
		if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				String str = file.getAbsolutePath();
				if (str.indexOf(".png") > 0) {
					str = str.substring(0, str.length()-4);
				}
				ImageIO.write(createImage(), "png", new File(str+".png"));
			} catch (IOException ex) {}
		}
	}
	
	public void load () {
		fileChooser.showOpenDialog(null);
		File imageFile = fileChooser.getSelectedFile();
		if (imageFile != null && imageFile.toString().indexOf(".png") >= 0) {
			try {
				loadedImage = ImageIO.read(imageFile);
			} catch (IOException e1) {}
			points = new Stack<Object>();
			repaint();
		} else {
			if (imageFile != null) {
				JOptionPane.showMessageDialog(null, "Wrong file type! Please select a .png file.");
			}
		}
	}
	
	public BufferedImage createImage () {
		int width = this.getWidth();
		int height = this.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		this.paint(g2);
		g2.dispose();
		
		return image;
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (drawingRectangle || drawingOval) {
			if (firstClick) {
				currentX = e.getX();
				currentY = e.getY();
				if (drawingRectangle) {
					currentShape = new Rectangle(currentX, currentY, currentColor, currentPenWidth, 0, 0);
				}
				else if (drawingOval) {
					currentShape = new Oval(currentX, currentY, currentColor, currentPenWidth, 0, 0);
				}
				firstClick = false;
			} else {
				currentWidth = Math.abs(e.getX()-currentX);
				currentHeight = Math.abs(e.getY()-currentY);
				currentShape.setWidth(currentWidth);
				currentShape.setHeight(currentHeight);
				if (currentX <= e.getX() && currentY >= e.getY()) {//up and left
					currentShape.setY(e.getY());
				}
				else if (currentX >= e.getX() && currentY <= e.getY()) {//down and right
					currentShape.setX(e.getX());
				}
				else if (currentX >= e.getX() && currentY >= e.getY()) {//up and right
					currentShape.setY(e.getY());
					currentShape.setX(e.getX());
				}
			}
		} else {
			currentStroke.add(new Point(e.getX(), e.getY(), currentColor, currentPenWidth));
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (drawingRectangle || drawingOval) {
			points.push(currentShape);
			firstClick = true;
			currentShape = null;
		} else {
			points.push(currentStroke);
			currentStroke = new ArrayList<Point>();
		}
		undoRedoStack = new Stack<Object>();
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			if (e.getKeyCode() == KeyEvent.VK_Z) {
				undo();
			}
			if (e.getKeyCode() == KeyEvent.VK_Y) {
				redo();
			}
		}		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main (String[] args) {
		PaintProgram app = new PaintProgram();
	}
	
	public class Point {
		
		int x, y;
		Color color;
		float penWidth;
		
		public Point (int x, int y, Color color, float penWidth) {
			this.x = x;
			this.y = y;
			this.color = color;
			this.penWidth = penWidth;
		}
		
		public int getX () {
			return this.x;
		}
		
		public int getY () {
			return this.y;
		}
		
		public Color getColor () {
			return this.color;
		}
		
		public float getPenWidth () {
			return this.penWidth;
		}
		
		public void setX (int x) {
			this.x = x;
		}
		
		public void setY (int y) {
			this.y = y;
		}
	}
	
	public class Shape extends Point {
		

		private int width, height;
		
		public Shape (int x, int y, Color color, float penWidth, int width, int height) {
			super(x, y, color, penWidth);
			this.width = width;
			this.height = height;
		}
		
		public void setWidth (int w) {
			this.width = w;
		}
		
		public void setHeight (int h) {
			this.height = h;
		}
		
		public int getWidth () {
			return this.width;
		}
		
		public int getHeight () {
			return this.height;
		}
		
	}
	
	public class Rectangle extends Shape {

		public Rectangle (int x, int y, Color color, float penWidth, int width, int height) {
			super(x, y, color, penWidth, width, height);
		}
		
		public Rectangle2D.Double getShape () {
			return new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
		}
	}
	
	public class Oval extends Shape {

		public Oval (int x, int y, Color color, float penWidth, int width, int height) {
			super(x, y, color, penWidth, width, height);
		}
		
		public Ellipse2D.Double getShape () {
			return new Ellipse2D .Double(getX(), getY(), getWidth(), getHeight());
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
