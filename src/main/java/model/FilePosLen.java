package model;

// position is the index of the first block of a file in a node (columns of x)
// fileLen is the number of blocks of a file
// weight is the co-occurence value of a block (equal for every blocks of a file)
public class FilePosLen {

    int[] position;
    int[] fileLen;
    int weight;

    public FilePosLen(int[] position, int[] fileLen, int weight) {
        this.position = position;
        this.fileLen = fileLen;
        this.weight = weight;
    }

    public int[] getPosition() {
        return position;
    }

    public int[] getFileLen() {
        return fileLen;
    }

    public int getWeight() {
        return weight;
    }

    public static FilePosLen merge(FilePosLen filePosLen1, FilePosLen filePosLen2) {
        int[] pos1 = filePosLen1.getPosition();
        int[] pos2 = filePosLen2.getPosition();
        int[] len1 = filePosLen1.getFileLen();
        int[] len2 = filePosLen2.getFileLen();
        int[] pos = new int[pos1.length + pos2.length];
        int[] len = new int[len1.length + len2.length];
        System.arraycopy(pos1, 0, pos, 0, pos1.length);
        System.arraycopy(pos2, 0, pos, pos1.length, pos2.length);
        System.arraycopy(len1, 0, len, 0, len1.length);
        System.arraycopy(len2, 0, len, len1.length, len2.length);
        return new FilePosLen(pos, len, filePosLen1.getWeight());
    }
    
}
