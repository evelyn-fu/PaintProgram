import java.awt.event.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Stack;

public class PaintProgram extends JPanel implements MouseMotionListener, ActionListener, MouseListener, AdjustmentListener, ChangeListener, KeyListener{
    private FreeLine currentFreeLine;
    private Block currentRect;
    private Oval currentOval;
    private Line currentLine;
    private ArrayList<Shape> shapes;
    private Stack<Shape> strokes;
    private Stack<Shape> undid;
    JFrame frame;
    JPanel penWidthPanel;
    JMenuBar menu;
    JMenu colorMenu;
    Color[] colors = new Color[] {Color.WHITE, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.MAGENTA, Color.PINK, Color.CYAN, Color.GRAY, Color.BLACK};
    JButton freeDraw, rectangleTool, ovalTool, lineTool, undo, redo;
    JScrollBar penWidthBar;
    JLabel penWidthLabel;
    JColorChooser colorChooser;
    Color currentColor = Color.RED;
    int currentWidth = 5;
    int anchorX, anchorY;
    String mode;
    boolean enterPressed = false, ctrlPressed = false;

    public PaintProgram(){
        shapes = new ArrayList<Shape>();
        strokes = new Stack<Shape>();
        undid = new Stack<Shape>();
        frame = new JFrame("Paint Program");
        setFocusable(true);
        requestFocusInWindow();
        frame.add(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mode = "free";

        menu = new JMenuBar();
        colorMenu = new JMenu("Pen Colors");

        for(int i=0; i<colors.length; i++){
            JMenuItem c = new JMenuItem();
            c.setBackground(colors[i]);
            colorMenu.add(c);
            final int j = i;
            c.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    currentColor = colors[j];
                }
            });
        }

        colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(this);
        colorMenu.add(colorChooser);

        menu.add(colorMenu);

        freeDraw = new JButton("Free Draw");
        freeDraw.addActionListener(this);
        menu.add(freeDraw);

        rectangleTool = new JButton("Rectangle");
        rectangleTool.addActionListener(this);
        menu.add(rectangleTool);

        ovalTool = new JButton("Oval");
        ovalTool.addActionListener(this);
        menu.add(ovalTool);

        lineTool = new JButton("Line");
        lineTool.addActionListener(this);
        menu.add(lineTool);

        undo = new JButton("Undo");
        undo.addActionListener(this);
        menu.add(undo);

        redo = new JButton("Redo");
        redo.addActionListener(this);
        menu.add(redo);

        penWidthBar = new JScrollBar(JScrollBar.HORIZONTAL,0,0,0,19);
        penWidthBar.setValue(4);
        penWidthBar.addAdjustmentListener(this);

        penWidthLabel = new JLabel("Pen Width: "+currentWidth+" ");

        penWidthPanel = new JPanel();
        penWidthPanel.setLayout(new BorderLayout());
        penWidthPanel.add(penWidthLabel, BorderLayout.WEST);
        penWidthPanel.add(penWidthBar, BorderLayout.CENTER);

        menu.add(penWidthPanel);

        frame.add(menu, BorderLayout.NORTH);
        
        frame.setVisible(true);
    }

    public void mouseClicked (MouseEvent mouseEvent) {} 
    public void mouseEntered (MouseEvent mouseEvent) {} 
    public void mousePressed (MouseEvent mouseEvent) {
        if(mode.equals("free")){
            currentFreeLine = new FreeLine(currentColor, currentWidth);
            shapes.add(currentFreeLine);
            strokes.add(currentFreeLine);
        }
        if(mode.equals("rectangle")){
            anchorX = mouseEvent.getX();
            anchorY = mouseEvent.getY();
            currentRect = new Block(anchorX, anchorY, 0, 0, currentColor, currentWidth);
            shapes.add(currentRect);
            strokes.add(currentRect);
        }
        if(mode.equals("oval")){
            anchorX = mouseEvent.getX();
            anchorY = mouseEvent.getY();
            currentOval = new Oval(anchorX, anchorY, 0, 0, currentColor, currentWidth);
            shapes.add(currentOval);
            strokes.add(currentOval);
        }
        if(mode.equals("line")){
            anchorX = mouseEvent.getX();
            anchorY = mouseEvent.getY();
            currentLine = new Line(anchorX, anchorY, anchorX, anchorY, currentColor, currentWidth);
            shapes.add(currentLine);
            strokes.add(currentLine);
        }
        undid = new Stack<Shape>();
    } 
    public void mouseReleased (MouseEvent mouseEvent) {}  
    public void mouseExited (MouseEvent mouseEvent) {} 

    public void mouseMoved(MouseEvent e){}

    public void mouseDragged(MouseEvent e){
        if(mode.equals("free")){
            currentFreeLine.addPoint(new Point(e.getX(), e.getY(), currentColor, currentWidth));
        }
        if(mode.equals("rectangle") || mode.equals("oval")){
            Shape currentShape = currentRect;
            if(mode.equals("oval"))
                currentShape = currentOval;

            if((e.getX()-anchorX)>0 && (e.getY()-anchorY)>0){
                currentShape.setX(anchorX);
                currentShape.setY(anchorY);
            }
            else if((e.getX()-anchorX)>0){
                currentShape.setX(anchorX);
                currentShape.setY(e.getY());
            }
            else if((e.getY()-anchorY)>0){
                currentShape.setX(e.getX());
                currentShape.setY(anchorY);
            }
            else{
                currentShape.setX(e.getX());
                currentShape.setY(e.getY());
            }

            if(enterPressed){
                currentShape.setWidth(Math.max(Math.abs(anchorX-e.getX()), Math.abs(anchorY-e.getY())));
                currentShape.setHeight(Math.max(Math.abs(anchorX-e.getX()), Math.abs(anchorY-e.getY())));
            }
            else{
                currentShape.setWidth(Math.abs(anchorX-e.getX()));
                currentShape.setHeight(Math.abs(anchorY-e.getY()));
            }
        }
        if(mode.equals("line")){
            currentLine.setWidth(e.getX());
            currentLine.setHeight(e.getY());
        }

        
        frame.validate();
        repaint();
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource()==freeDraw){
            mode = "free";
        }
        if(e.getSource()==rectangleTool){
            mode = "rectangle";
        }
        if(e.getSource()==ovalTool){
            mode = "oval";
        }
        if(e.getSource()==lineTool){
            mode = "line";
        }
        if(e.getSource()==undo){
            if(strokes.size() != 0){
                Shape undidShape = strokes.pop();
                undid.add(undidShape);
                shapes.remove(undidShape);
                repaint();
            }
        }
        if(e.getSource()==redo){
            if(undid.size() != 0){
                Shape redidShape = undid.pop();
                strokes.add(redidShape);
                shapes.add(redidShape);
                repaint();
            }
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e){
        if(e.getSource()==penWidthBar){
            currentWidth = penWidthBar.getValue()+1;
            penWidthLabel.setText("Pen Width: "+currentWidth+" ");
        }
    }

    public void stateChanged(ChangeEvent e){
        currentColor = colorChooser.getColor();
    }

    public void keyPressed(KeyEvent e){
        System.out.println("pressed");
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            enterPressed = true;
        }
        if(e.getKeyCode() == KeyEvent.VK_CONTROL){
            ctrlPressed = true;
        }
    }
    public void keyReleased(KeyEvent e){
        System.out.println("released");
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            enterPressed = false;
        }
        if(e.getKeyCode() == KeyEvent.VK_CONTROL){
            ctrlPressed = false;
        }
    } 
    public void keyTyped(KeyEvent e){
        System.out.println("typed");
        if(e.getKeyCode() == KeyEvent.VK_C && ctrlPressed){
            undo.doClick();
        }
        if(e.getKeyCode() == KeyEvent.VK_V && ctrlPressed){
            redo.doClick();
        }
    }

    public void paintComponent(Graphics g){
        System.out.println(frame.getFocusOwner()+" "+this);
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(Color.BLACK);
        g2.fill(new Rectangle(0, 0, frame.getWidth(), frame.getHeight()));
        for(Shape s : shapes){
            g2.setColor(s.getColor());
            g2.setStroke(new BasicStroke(s.getPenWidth()));
            if(s instanceof FreeLine){
                FreeLine l = (FreeLine)s;
                if(l.getPoints().size() != 0){
                    Point p1 = l.getPoints().get(0);
                    for(Point p : l.getPoints()){
                        if(p1 != p){
                            g2.drawLine(p1.getX(), p1.getY(), p.getX(), p.getY());
                            p1=p;
                        }
                    }
                }
            }
            if(s instanceof Block)
                g2.draw(((Block)s).getRect());
            if(s instanceof Oval)
                g2.draw(((Oval)s).getOval());
            if(s instanceof Line)
                g2.draw(((Line)s).getLine());
        }
    }

    public class Point{
        private int x, y, penWidth;
        private Color c;
        public Point(int x, int y, Color c, int penWidth){
            this.x = x;
            this.y = y;
            this.c = c;
            this.penWidth = penWidth;
        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
        public Color getColor(){
            return c;
        }
        public int getPenWidth(){
            return penWidth;
        }
    }

    public class Shape{
        private int x, y, width, height, penWidth;
        private Color color;
        public Shape(int x, int y, int width, int height, Color color, int penWidth){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.penWidth = penWidth;
        }
        public int getX(){
            return x;
        }
        public int getY(){
            return y;
        }
        public int getWidth(){
            return width;
        }
        public int getHeight(){
            return height;
        }
        public Color getColor(){
            return color;
        }
        public int getPenWidth(){
            return penWidth;
        }
        public void setX(int newX){
            x = newX;
        }
        public void setY(int newY){
            y = newY;
        }
        public void setWidth(int w){
            width = w;
        }
        public void setHeight(int h){
            height = h;
        }
    }

    public class FreeLine extends Shape{
        private ArrayList<Point> points;
        public FreeLine(Color color, int penWidth){
            super(0, 0, 0, 0, color, penWidth);
            points = new ArrayList<Point>();
        }
        public ArrayList<Point> getPoints(){
            return points;
        }
        public void addPoint(Point p){
            points.add(p);
        }
    }

    public class Block extends Shape{
        public Block(int x, int y, int width, int height, Color color, int penWidth){
            super(x, y, width, height, color, penWidth);
        }
        public Rectangle getRect(){
            return new Rectangle(getX(), getY(), getWidth(), getHeight());
        }
    }

    public class Oval extends Shape{
        public Oval(int x, int y, int width, int height, Color color, int penWidth){
            super(x, y, width, height, color, penWidth);   
        }
        public Ellipse2D.Double getOval(){
            return new Ellipse2D.Double(getX(), getY(), getWidth(), getHeight());
        }
    }

    public class Line extends Shape{
        public Line(int x1, int y1, int x2, int y2, Color color, int penWidth){
            super(x1, y1, x2, y2, color, penWidth);   
        }
        public Line2D.Double getLine(){
            return new Line2D.Double(getX(), getY(), getWidth(), getHeight());
        }
    }
    
    public static void main(String[]args){
        PaintProgram app = new PaintProgram();
    }
}