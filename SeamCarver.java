import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.util.*;

public class SeamCarver {
    /***
     * the main idea of this project is to find the lowest color difference path in a image
     * we call this path as seam and remove this seam from the image
     * @param args
     */
    public static void main(String[] args) {
        String inputpath = null;
        String outputpath = null;
        String direction = null;
        Integer num = null;
        String method = null;
        Scanner s = new Scanner(System.in);

        //make sure the input path and output path is valid
        System.out.print("Please type the input path of image：");
        inputpath = s.nextLine();
        System.out.print("Please type the output path of image：");
        outputpath = s.nextLine();
        if (inputpath == null && outputpath == null) {
            System.err.println("input path or output path is empty");
            return;
        }
        BufferedImage image;
        try {
            image = ImageIO.read(new File(inputpath));
        } catch (IOException e) {
            System.err.println("Can't open " + inputpath);
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();

        // make which method user want to use
        System.out.print("Please type the method you want to use, D means Dynamic Programming, R means Recursive");
        method = s.nextLine();
        if(!method.equals("D") && !method.equals("R") )
        {
            System.err.println("Please return the right method.");
            return;
        }
        if(method.equals("D")){
            System.out.print("Please type the method of Dynamic Programming you want to use, T means Top-Down, B means Bottom_Up");
            String DPMethod = s.nextLine();
            if(!DPMethod.equals("T") && !DPMethod.equals("B") )
            {
                System.err.println("Please return the right method of Dynamic Programming.");
                return;
            }
            method += DPMethod;
        }

        //make sure the number of seams is valid, and make sure which direction want to resize
        System.out.println("the height of image is " + height +", and the width of image is " +width);
        System.out.print("please choose a direction of resize, vertical(v) or horizontal(h), return 'v' or 'h' ");
        direction = s.nextLine();
        if(!direction.equals("v") && !direction.equals("h") )
        {
            System.err.println("Please return the right letter.");
            return;
        }
        String dir = null;
        if(direction.equals("v")){
            dir = "height";
        }else
        {
            dir = "width";
        }
        System.out.print("Please type the number of seam(the number should smaller than "+ dir +"：");
        num = s.nextInt();
        if (num <= 0) {
            System.err.println("the number of seams is smaller than 0");
            System.err.println("number of seams needs to be a positive integer.");
            return;
        }

        //seam carve the image n times, n = number
        BufferedImage newImage = image;
        while (num > 0) {
            System.out.println(num);
            newImage = SeamCarve(newImage,direction,method);

            num = num - 1;
        }
        try {
            File outputfile = new File(outputpath);
            ImageIO.write(newImage, "png", outputfile);
        } catch (IOException e) {
            System.err.println("Trouble saving " + outputpath);
            return;
        }

        //show old image and new image
        showImage(image);
        showImage(newImage);
    }

    /**
     * from the direction to decide use which function: vertical or horizontal
     * and from method to decide which dynamic programming method to use, top_down or bottom_up
     * @param image
     * @param direction
     * @return newimage
     */
    private static BufferedImage SeamCarve(BufferedImage image,String direction,String method) {
        // We need to compute the energy table, find and remove a seam.
        BufferedImage newImage = null;
        double[][] energyTable = ComputeColordifference(image);
        //after we compute the energy table, use if and some methods to find the seam.
        if(direction.equals("h") && method.equals("DB")) {
            int[][] seam = horizontalResizeBU(energyTable);
            newImage = deleteSeam(image, seam, direction);
        }else if(direction.equals("v")&& method.equals("DB")) {
            int[][] seam = verticalResizeBU(energyTable);
            newImage = deleteSeam(image, seam, direction);
        }else if(direction.equals("h")&& method.equals("DT")) {
            int[][] seam = horizontalResizeTD(energyTable);
            newImage = deleteSeam(image, seam, direction);
        }else if(direction.equals("v")&& method.equals("DT")) {
            int[][] seam = verticalResizeTD(energyTable);
            newImage = deleteSeam(image, seam, direction);
        }else if(direction.equals("h")&& method.equals("R")) {
            ArrayList<Integer> seamlist = horizontalResizeRC(energyTable);
            int[][] seam = hListToArray(seamlist);
            newImage = deleteSeam(image, seam, direction);
        }else if(direction.equals("v")&& method.equals("R")) {
            ArrayList<Integer> seamlist = verticalResizeRC(energyTable);
            int[][] seam = vListToArray(seamlist);
            newImage = deleteSeam(image, seam, direction);
        }
        return newImage;
    }

    /**
     * make a two dimension array, and get the color difference value in each pixel,
     * if the color is similar the value should be close to 0
     * otherwise the value should be higher
     * @param image
     * @return
     */
    private static double[][] ComputeColordifference(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] colorDifTable = new double[width][height];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int row1;
                int row2;
                int column1;
                int column2;

                // we should consider the boundary of the two dimension array
                // when i==0 || i==width-1 || j==0 || j==height-1 compare the boundary and the position next to them
                // otherwise compare the position next to target like (i-1 and i+1)
                if (i == 0) {
                    // leftmost column
                    row1 = image.getRGB(i, j);
                    row2 = image.getRGB(i + 1, j);
                } else if (i == width - 1) {
                    // rightmost column
                    row1 = image.getRGB(i - 1, j);
                    row2 = image.getRGB(i, j);
                } else {
                    // middle columns
                    row1 = image.getRGB(i - 1, j);
                    row2 = image.getRGB(i + 1, j);
                }

                if (j == 0) {
                    // bottom row
                    column1 = image.getRGB(i, j);
                    column2 = image.getRGB(i, j + 1);
                } else if (j == height - 1) {
                    // top row
                    column1 = image.getRGB(i, j - 1);
                    column2 = image.getRGB(i, j);
                } else {
                    // middle rows
                    column1 = image.getRGB(i, j - 1);
                    column2 = image.getRGB(i, j + 1);
                }

                // we will use bitwise to find the difference of the color
                // and the optical three prime color is red, green and blue
                // and their binary representation is 00ff0000, 0000ff00, 00000ff
                // we compute the difference in each part and use the sum as the difference
                int RowRed = Math.abs(((row1 & 0x00ff0000) >> 16) - ((row2 & 0x00ff0000) >> 16));
                int RowGreen = Math.abs(((row1 & 0x0000ff00) >> 8) - ((row2 & 0x0000ff00) >> 8));
                int RowBlue = Math.abs((row1 & 0x000000ff) - (row2 & 0x000000ff));
                int ColumnRed = Math.abs(((column1 & 0x00ff0000) >> 16) - ((column2 & 0x00ff0000) >> 16));
                int ColumnGreen = Math.abs(((column1 & 0x0000ff00) >> 8) - ((column2 & 0x0000ff00) >> 8));
                int ColumnBlue = Math.abs((column1 & 0x000000ff) - (column2 & 0x000000ff));
                double colorDifference = RowRed + RowGreen + RowBlue + ColumnRed + ColumnGreen + ColumnBlue;
                colorDifTable[i][j] = colorDifference;
            }
        }

        return colorDifTable;
    }

    /***
     * Dynamic programming part
     */

    /***
     * Bottom_Up part
     */

    /***
     * use dynamic programming to find the seam
     * horizontal resize means the new image's width will smaller than original image
     * @param colorDifTable
     * @return
     */
    private static int[][] horizontalResizeBU(double[][] colorDifTable) {
        int[][] seam;
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double[][] DP = new double[width][height];
        int[][] helper = new int[width][height];
        double minimum;
        seam = new int[colorDifTable[0].length][2];
        // find the lowest color difference path in color difference table
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                if (j == 0) {
                    DP[i][j] = colorDifTable[i][j];
                    helper[i][j] = -1;
                } else {
                    if (i == 0) {
                        minimum = Math.min(DP[i][j - 1], DP[i + 1][j - 1]);
                        if (minimum == DP[i][j - 1]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 2;
                        }
                    } else if (i == (width - 1)) {
                        minimum = Math.min(DP[i][j - 1], DP[i - 1][j - 1]);
                        if (minimum == DP[i][j - 1]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 0;
                        }
                    } else {
                        minimum = Math.min(DP[i - 1][j - 1], Math.min(DP[i][j - 1], DP[i + 1][j - 1]));
                        if (minimum == DP[i - 1][j - 1]) {
                            helper[i][j] = 0;
                        } else if (minimum == DP[i][j - 1]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 2;
                        }
                    }
                    DP[i][j] = colorDifTable[i][j] + minimum;
                }
            }
        }
        double min_num = DP[0][height - 1];
        int min_index = 0;
        for (int x = 0; x < width; x++) {
            if (min_num > DP[x][height - 1]) {
                min_index = x;
                min_num = DP[x][height - 1];
            }
        }

        // now that we have the min we need to backtrace it.
        // min_index is the end of the lowest color difference seam.
        int j_index = height - 1;
        int i_index = min_index;
        seam[j_index][0] = i_index;
        seam[j_index][1] = j_index;
        int help;
        while (j_index > 0) {
            help = helper[i_index][j_index];
            if (help != -1) {
                if (help == 0) {
                    i_index = i_index - 1;
                } else if (help == 1) {
                    i_index = i_index;
                } else {
                    i_index = i_index + 1;
                }
            } else {
                i_index = i_index;
            }
            j_index = j_index - 1;

            seam[j_index][0] = i_index;
            seam[j_index][1] = j_index;
        }
        return seam;
    }

    /***
     * use dynamic programming to find the seam
     * vertical resize means the new image's height will smaller than original image
     *
     * @param colorDifTable
     * @return
     */
    private static int[][] verticalResizeBU(double[][] colorDifTable) {
        int[][] seam;
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double[][] DP = new double[width][height];
        int[][] helper = new int[width][height];
        double minimum;
        seam = new int[colorDifTable.length][2];
        // find the lowest color difference path in color difference table
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0) {
                    DP[i][j] = colorDifTable[i][j];
                    helper[i][j] = -1;
                } else {
                    if (j == 0) {
                        minimum = Math.min(DP[i - 1][j], DP[i - 1][j + 1]);
                        if (minimum == DP[i - 1][j]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 2;
                        }
                    } else if (j == (height - 1)) {
                        minimum = Math.min(DP[i - 1][j], DP[i - 1][j - 1]);
                        if (minimum == DP[i - 1][j]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 0;
                        }
                    } else {
                        minimum = Math.min(DP[i - 1][j - 1], Math.min(DP[i - 1][j], DP[i - 1][j + 1]));
                        if (minimum == DP[i - 1][j - 1]) {
                            helper[i][j] = 0;
                        } else if (minimum == DP[i - 1][j]) {
                            helper[i][j] = 1;
                        } else {
                            helper[i][j] = 2;
                        }
                    }
                    DP[i][j] = colorDifTable[i][j] + minimum;
                }
            }
        }
        double min_num = DP[width-1][0];
        int min_index = 0;
        for (int j = 0; j < height; j++) {
            if (min_num > DP[width - 1][j]) {
                min_index = j;
                min_num = DP[width - 1][j];
            }
        }

        // now that we have the min we need to backtrace it.
        // min_index is the end of the lowest color difference seam.
        int j_index = min_index;
        int i_index = width - 1;
        seam[i_index][0] = i_index;
        seam[i_index][1] = j_index;
        int help;
        while (i_index > 0) {
            help = helper[i_index][j_index];
            if (help != -1) {
                if (help == 0) {
                    j_index = j_index - 1;
                } else if (help == 1) {
                    j_index = j_index;
                } else { // = 2
                    j_index = j_index + 1;
                }
            } else {
                j_index = j_index;
            }
            i_index = i_index - 1;

            seam[i_index][0] = i_index;
            seam[i_index][1] = j_index;
        }
        return seam;
    }


    /***
     * Top-Down part
     */

    /***
     * horizontal resize by dynamic programming
     * @param colorDifTable
     * @return
     */
    private static int[][] horizontalResizeTD(double[][] colorDifTable) {
        int[][] seam;
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double[][] DP = new double[width][height];
        int[][] helper = new int[width][height];
        seam = new int[colorDifTable[0].length][2];
        for(int i = 0; i < width; i++){
            Arrays.fill(DP[i],-1.0);
        }
        for (int i = 0; i < width; i++) {
            DP[i][0] = colorDifTable[i][0];
            helper[i][0] = -1;
        }
        for (int i = width - 1; i >= 0; i--) {
            DP[i][height-1] = minHorizontalDifTopDown(i,height-1, colorDifTable, DP, helper);
        }
        double min_num = DP[0][height - 1];
        int min_index = 0;
        for (int x = 0; x < width; x++) {
            if (min_num > DP[x][height - 1]) {
                min_index = x;
                min_num = DP[x][height - 1];
            }
        }
        // now that we have the min we need to backtrace it.
        // min_index is the end of the lowest color difference seam.
        int j_index = height - 1;
        int i_index = min_index;
        seam[j_index][0] = i_index;
        seam[j_index][1] = j_index;
        int help;
        while (j_index > 0) {
            help = helper[i_index][j_index];
            if (help != -1) {
                if (help == 0) {
                    i_index = i_index - 1;
                } else if (help == 1) {
                    i_index = i_index;
                } else {
                    i_index = i_index + 1;
                }
            } else {
                i_index = i_index;
            }
            j_index = j_index - 1;

            seam[j_index][0] = i_index;
            seam[j_index][1] = j_index;
        }
        return seam;
    }

    /***
     * The progress of horizontal dynamic programming Top-Down with memoization.
     * @param i
     * @param j
     * @param colorDifTable
     * @param DP
     * @param helper
     * @return
     */
    private static double  minHorizontalDifTopDown(int i, int j, double[][] colorDifTable, double[][] DP, int[][] helper){
        if(DP[i][j]>=0){
            return DP[i][j];
        }
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double minimum;
        if (i == 0) {
            minimum = Math.min(minHorizontalDifTopDown(i,j - 1, colorDifTable, DP, helper), minHorizontalDifTopDown(i + 1,j - 1, colorDifTable, DP, helper));
            if (minimum == DP[i][j - 1]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 2;
            }
        } else if (i == (width - 1)) {
            minimum = Math.min(minHorizontalDifTopDown(i,j - 1, colorDifTable, DP, helper), minHorizontalDifTopDown(i - 1,j - 1, colorDifTable, DP, helper));
            if (minimum == DP[i][j - 1]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 0;
            }
        } else {
            minimum = Math.min(minHorizontalDifTopDown(i - 1,j - 1, colorDifTable, DP, helper), Math.min(minHorizontalDifTopDown(i,j-1, colorDifTable, DP, helper),minHorizontalDifTopDown(i + 1,j - 1, colorDifTable, DP, helper)));
            if (minimum == DP[i - 1][j - 1]) {
                helper[i][j] = 0;
            } else if (minimum == DP[i][j - 1]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 2;
            }
        }
        DP[i][j] = minimum + colorDifTable[i][j];
        return DP[i][j];
    }

    /***
     * vertical resize by dynamic programming
     * @param colorDifTable
     * @return
     */
    private static int[][] verticalResizeTD(double[][] colorDifTable) {
        int[][] seam;
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double[][] DP = new double[width][height];
        int[][] helper = new int[width][height];
        double minimum;
        seam = new int[colorDifTable.length][2];
        for(int i = 0; i < width; i++){
            Arrays.fill(DP[i],-1.0);
        }
        for (int j = 0; j < height; j++) {
            DP[0][j] = colorDifTable[0][j];
            helper[0][j] = -1;
        }
        for (int j = height - 1; j >= 0; j--) {
            DP[width - 1][j] = minVerticalDifTopDown(width - 1, j, colorDifTable, DP, helper);
        }
        double min_num = DP[width-1][0];
        int min_index = 0;
        for (int j = 0; j < height; j++) {
            if (min_num > DP[width - 1][j]) {
                min_index = j;
                min_num = DP[width - 1][j];
            }
        }
        // now that we have the min we need to backtrace it.
        // min_index is the end of the lowest color difference seam.
        int j_index = min_index;
        int i_index = width - 1;
        seam[i_index][0] = i_index;
        seam[i_index][1] = j_index;
        int help;
        while (i_index > 0) {
            help = helper[i_index][j_index];
            if (help != -1) {
                if (help == 0) {
                    j_index = j_index - 1;
                } else if (help == 1) {
                    j_index = j_index;
                } else { // = 2
                    j_index = j_index + 1;
                }
            } else {
                j_index = j_index;
            }
            i_index = i_index - 1;

            seam[i_index][0] = i_index;
            seam[i_index][1] = j_index;
        }
        return seam;
    }

    /***
     * The progress of vertical dynamic programming Top-Down with memoization.
     * @param i
     * @param j
     * @param colorDifTable
     * @param DP
     * @param helper
     * @return
     */
    private static double  minVerticalDifTopDown(int i, int j, double[][] colorDifTable, double[][] DP, int[][] helper){
        if(DP[i][j]>=0){
            return DP[i][j];
        }
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        double minimum;
        if (j == 0) {
            minimum = Math.min(minVerticalDifTopDown(i - 1, j,colorDifTable, DP, helper),minVerticalDifTopDown(i - 1, j + 1,colorDifTable, DP, helper));
            if (minimum == DP[i - 1][j]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 2;
            }
        } else if (j == (height - 1)) {
            minimum = Math.min(minVerticalDifTopDown(i - 1, j,colorDifTable, DP, helper),minVerticalDifTopDown(i - 1, j - 1,colorDifTable, DP, helper));
            if (minimum == DP[i - 1][j]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 0;
            }
        } else {
            minimum = Math.min(minVerticalDifTopDown(i - 1, j - 1, colorDifTable, DP, helper), Math.min(minVerticalDifTopDown(i - 1, j,colorDifTable, DP, helper),minVerticalDifTopDown(i - 1, j + 1,colorDifTable, DP, helper)));
            if (minimum == DP[i - 1][j - 1]) {
                helper[i][j] = 0;
            } else if (minimum == DP[i - 1][j]) {
                helper[i][j] = 1;
            } else {
                helper[i][j] = 2;
            }
        }
        DP[i][j] = colorDifTable[i][j] + minimum;
        return DP[i][j];
    }



    /***
     * Recursive part
     */
    public static double HRCMinimun = 0;
    public static ArrayList<Integer> HRCseamList;
    /***
     * redefine the value of HRCMinimum and use HRC from every point in the first row
     * @param colorDifTable
     * @return
     */
    private static ArrayList horizontalResizeRC(double[][] colorDifTable) {
        int width = colorDifTable.length;
        HRCMinimun = Double.MAX_VALUE;
        ArrayList<Integer> curlist = new ArrayList();
        for(int i = 0; i < width;i++){
            curlist.add(i);
            ArrayList<Integer> newcur = new ArrayList(curlist);
            HRC(i,1, 0+ colorDifTable[i][0], newcur, colorDifTable);
            curlist.remove(curlist.size()-1);
        }
        return curlist;
    }

    /***
     * recursive algorithm when horizontal resize, it will end if it reach the last row.
     * @param i
     * @param j
     * @param cur
     * @param curlist
     * @param colorDifTable
     */
    private static void HRC(int i, int j,  double cur, ArrayList<Integer> curlist, double[][] colorDifTable){
        if(j==colorDifTable[0].length){
            if(cur<=HRCMinimun){
                System.out.println(curlist);
                HRCMinimun = cur;
                HRCseamList = new ArrayList(curlist);
            }
            return;
        }
        for(int c = -1; c<=1 ; c++){
            if(i + c >= 0 && i + c < colorDifTable.length){
                curlist.add(c);
                ArrayList<Integer> newcur = new ArrayList(curlist);
                HRC(i+c, j+1, cur + colorDifTable[i + c][j], newcur, colorDifTable);
                curlist.remove(curlist.size()-1);
            }
        }
    }

    /***
     * change the represent of seam, transfer list to Array
     * @param curlist
     * @return
     */
    private static int[][] hListToArray(ArrayList<Integer> curlist){
        int[][] seam = new int[curlist.size()][2];
        int start = curlist.get(0);
        seam[0][0] = start;
        seam[0][1] = 0;
        for(int j = 1;j < seam.length;j++){
            seam[j][0] = seam[j-1][0] + curlist.get(j);
            seam[j][1] = j;
        }
        return seam;
    }

    public static double VRCMinimun = 0;
    public static ArrayList<Integer> VRCseamList;
    /***
     * redefine the value of VRCMinimum and use VRC from every point in the first column
     * @param colorDifTable
     * @return
     */
    private static ArrayList verticalResizeRC(double[][] colorDifTable) {
        int width = colorDifTable.length;
        int height = colorDifTable[0].length;
        VRCMinimun = Double.MAX_VALUE;
        ArrayList<Integer> curlist = new ArrayList();
        for(int j = 0; j < height;j++){
            curlist.add(j);
            ArrayList<Integer> newcur = new ArrayList(curlist);
            VRC(1, j, 0+ colorDifTable[0][j], newcur, colorDifTable);
            curlist.remove(curlist.size()-1);
        }
        return curlist;
    }

    /***
     * recursive algorithm when vertical resize, it will end if it reach the last column.
     * @param i
     * @param j
     * @param cur
     * @param curlist
     * @param colorDifTable
     */
    private static void VRC(int i, int j,  double cur, ArrayList<Integer> curlist, double[][] colorDifTable){
        if(j==colorDifTable[0].length){
            if(cur<HRCMinimun){
                HRCMinimun = cur;
                VRCseamList = new ArrayList(curlist);
            }
            return;
        }
        for(int c = -1; c<=1 ; c++){
            if(i + c >= 0 && i + c < colorDifTable.length){
                curlist.add(c);
                ArrayList<Integer> newcur = new ArrayList(curlist);
                HRC(i+1, j+c, cur + colorDifTable[i][j+c], newcur, colorDifTable);
                curlist.remove(curlist.size()-1);
            }
        }
    }

    /***
     * change the represent of seam, transfer list to Array
     * @param curlist
     * @return
     */
    private static int[][] vListToArray(ArrayList<Integer> curlist){
        int[][] seam = new int[curlist.size()][2];
        int start = curlist.get(0);
        seam[0][0] = 0;
        seam[0][1] = start;
        for(int i = 1;i < seam.length;i++){
            seam[i][0] = i;
            seam[i][1] = seam[i-1][0] + curlist.get(i);
        }
        return seam;
    }

    /***
     *  delete the seam in the image
     * @param image
     * @param seam
     * @param direction
     * @return
     */
    private static BufferedImage deleteSeam(BufferedImage image, int[][] seam,String direction) {
        BufferedImage newImage;
        int width = image.getWidth();
        int height = image.getHeight();
        if (direction.equals("h")) {
            newImage = new BufferedImage(width - 1, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            newImage = new BufferedImage(width, height - 1, BufferedImage.TYPE_INT_ARGB);
        }
        // iterate every pixel in the original image and copies them.
        // Do not copy the pixels in the seam.
        if(direction.equals("h")) {
            for (int j = 0; j < height; j++) {
                boolean shift = false;
                for (int i = 0; i < width; i++) {
                    // Simple loop to check if the pixel is part of the seam or not.
                    boolean inSeam = false;
                    if ((seam[j][0] == i) && (seam[j][1] == j)) {
                        inSeam = true;
                        shift = true;
                    }
                    if (!inSeam) {
                        // pixel not part of the seam, so we add it.
                        int color = image.getRGB(i, j);
                        if (shift) {
                            newImage.setRGB(i - 1, j, color);
                        } else {
                            newImage.setRGB(i, j, color);
                        }
                    }
                }
            }
        }else{
            for (int i = 0; i < width; i++) {
                boolean shift = false;
                for (int j = 0; j < height; j++) {
                    // loop every pixel in the image, and check if it in the seam
                    boolean inSeam = false;
                    if ((seam[i][0] == i) && (seam[i][1] == j)) {
                        inSeam = true;
                        shift = true;
                    }
                    if (!inSeam) {
                        // pixel not part of the seam, add it.
                        if (shift) {
                            newImage.setRGB(i, j - 1, image.getRGB(i, j));
                        } else {
                            newImage.setRGB(i, j, image.getRGB(i, j));
                        }
                    }
                }
            }
        }

        return newImage;
    }

    /***
     * the function wo show the image
     *
     * @param image
     */
    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
    }
}