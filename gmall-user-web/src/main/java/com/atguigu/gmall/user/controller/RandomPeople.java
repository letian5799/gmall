package com.atguigu.gmall.user.controller;

import org.springframework.stereotype.Controller;

import java.util.Random;

@Controller
public class RandomPeople {
    public static void main(String[] args) {
        Random col = new Random();
        Random row = new Random();
        int randomRow = row.nextInt(4) + 1;
        int randomCol = col.nextInt(8) + 1;
        System.out.println("第" + row + "行，第" + col + "列");
    }

}
