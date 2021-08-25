package com.weiyuze.nettystudy.s02;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientFrame extends Frame {

    //把ClientFrame做成单例
    //一个Client窗口只需要有一个
    public static final ClientFrame INSTANCE = new ClientFrame();
    TextArea ta = new TextArea();//多行文本
    TextField tf = new TextField();//单行文本

    //保存Client中Channel对象，连接完毕之后进行初始化 1
    //窗口显示后调用connect连接，
    Client c = null;

    public ClientFrame() {
        this.setSize(600, 400);//大小
        this.setLocation(100, 20);//位置
        this.add(ta, BorderLayout.CENTER);//边界布局
        this.add(tf, BorderLayout.SOUTH);
        tf.addActionListener(new ActionListener() {//回车时触发事件
            @Override
            public void actionPerformed(ActionEvent e) {
                //把字符串发送到服务器 接收后对所有客户端转发一遍

                //处理actionPerformed事件时调用send发送
                //添加send(String msg)函数
                //用初始化好的channel进行传输
                c.send(tf.getText());
                //ta.setText(ta.getText() + tf.getText());
                tf.setText("");
            }
        });
        //this.setVisible(true);

        //窗口显示完毕后调用
        //new Client().connect();

        //保存Client中Channel对象，连接完毕之后进行初始化 2
        //connectToServer();
    }

    void connectToServer() {
        c = new Client();
        c.connect();
    }

    public static void main(String[] args) {
        //在main中拿到单例对象 显示
        ClientFrame frame = ClientFrame.INSTANCE;
        frame.setVisible(true);
        frame.connectToServer();
        //new ClientFrame();
    }

    //ClientHandler接收到数据后更新frame中的ta
    public void updateText(String msgAccepted) {
        ta.setText(ta.getText() + System.getProperty("line.separator") + msgAccepted);//System.getProperty()拿到属性的值；win: \r\n 换行回车 ;Linux \n 换行 换行符每种系统不一样 跨系统可这样写
    }
}
