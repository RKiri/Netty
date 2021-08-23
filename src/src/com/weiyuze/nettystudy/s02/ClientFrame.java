package com.weiyuze.nettystudy.s02;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientFrame extends Frame {
    TextArea ta = new TextArea();//多行文本
    TextField tf = new TextField();//单行文本

    public ClientFrame() {
        this.setSize(600, 400);//大小
        this.setLocation(100, 20);//位置
        this.add(ta, BorderLayout.CENTER);//边界布局
        this.add(tf, BorderLayout.SOUTH);
        tf.addActionListener(new ActionListener() {//回车时触发事件
            @Override
            public void actionPerformed(ActionEvent e) {
                //把字符串发送到服务器 接收后对所有客户端转发一遍
                ta.setText(ta.getText() + tf.getText());
                tf.setText("");
            }
        });
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new ClientFrame();
    }
}
