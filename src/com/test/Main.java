package com.test;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

//实现两个厨师，三个顾客吃饭的生产者消费者模型
public class Main {

    //生产者消费者模型 放东西的共享区域用队列实现
   public static final Queue<Object> queue = new LinkedList<>();

    public static void main(String[] args) {
        //建立两个生产者
        new Thread(Main::add, "厨师1").start();
        new Thread(Main::add, "厨师2").start();

        //create three comsumers
        new Thread(Main::take, "comsumer 1").start();
        new Thread(Main::take, "comsumer 2").start();
        new Thread(Main::take, "comsumer 3").start();
    }

    //厨师的生产方法 每三秒生产一次
    public static void add()
    {
        while(true)
        {
            try {
                Thread.sleep(3000);
                //和poll方法的作用是一样的 唯一的区别是offer方法当队列是空的时候会报异常
                // 由于是两个生产者 往队列里面放东西 所以需要对队列进行上锁(队列是共享的 一次只能被一个生产者使用
                synchronized (queue) {
                    String name = Thread.currentThread().getName();
                    System.out.println(new Date() + " " + name + "出餐");
                    queue.offer(new Object());
                    //此时的唤醒逻辑是写完消费者发现队列是空的时候，队列阻塞了，所以这里生产完之后需要唤醒操作
                    queue.notifyAll();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void take() {
       while(true)
       {
           //如果队列是空的话 那么消费者是没有办法拿东西的 此时队列需要暂时阻塞
           //这个时候队列处于阻塞状态，只能被唤醒 而上面的生产者生成完一个东西的时候就进行唤醒
           //所以此时需要完善代码 在生产者的代码中假如唤醒操作 所以唤醒的操作不是一开始就写好的 而是后面根据逻辑补充的
           //写完while就去生产者的offer操作后面添加notifyall代码
           try {
                synchronized (queue)
                {
                   while(queue.isEmpty()) {
                           queue.wait();
                   }
                   queue.poll();
                   String name = Thread.currentThread().getName();
                   System.out.println(new Date() + " " + name + "吃到饭了");
                    //这个时候看似代码写完了，但是考虑有多个消费者 而每一次只能有一个消费者进行消费
                    //所以对现在的代码需要进行加锁的操作 这个时候需要返回到while代码之前进行队列的加锁
               }
               Thread.sleep(4000);
           } catch (InterruptedException e) {
               throw new RuntimeException(e);
           }
        }
    }
}