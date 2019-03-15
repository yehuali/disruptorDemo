package com.example.test;

public class Test {
    public static void main(String[] args) {
//        Runnable run  =new Runnable() {
//            @Override
//            public void run() {
//                System.out.println("11111111111111111");
//            }
//        };
//        Thread thread = new Thread(run);
//        thread.start();
        int a = test1();

    }

    public static int test1(){
        for(int i =1;i<2;i++){
            if(true){
                return i -1;
            }
        }
        return 0;
    }
}
