package com.stkj.cashier.bean;


import java.util.Comparator;

public class UsernameComparator implements Comparator<FaceChooseItemEntity> {
    @Override
    public int compare(FaceChooseItemEntity o1, FaceChooseItemEntity o2) {
        return o1.getUsername().compareTo(o2.getUsername());
    }
}
