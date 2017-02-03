package mil.emp3.test.emp3vv.utils;

import java.util.Comparator;

/**
 *
 */
public class StringVersionComparator implements Comparator<String>{

    @Override
    public int compare(String lhs, String rhs) {
        int iCmp = 0;
        String[] aLeft;
        String[] aRight;
        int iLeft, iRight;
        int iMaxCmp;
        int iIndex;
        
        if ((lhs == null) || (rhs == null)) {
            throw new NullPointerException();
        }
        
        aLeft = lhs.split("\\.");
        aRight = rhs.split("\\.");
        
        if (aLeft.length <= aRight.length) {
            iMaxCmp = aLeft.length;
        } else {
            iMaxCmp = aRight.length;
        }
        
        for (iIndex = 0; iIndex < iMaxCmp; iIndex++) {
            try {
                iLeft = Integer.parseInt(aLeft[iIndex], 10);
                iRight = Integer.parseInt(aRight[iIndex], 10);
                
                iCmp = ((iLeft < iRight)? -1: ((iLeft > iRight)? 1: 0));
            } catch (NumberFormatException Ex) {
                iCmp = aLeft[iIndex].compareTo(aRight[iIndex]);
            }
            
            if (iCmp != 0) {
                break;
            }
        }
        
        if ((iCmp == 0) && (aLeft.length != aRight.length)) {
            iCmp = (aLeft.length < aRight.length)? -1: 1;
        }
        
        return iCmp;
    }
}
