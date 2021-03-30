/**  
 * @Title: LinkedList.java
 * @Package com.zq.utils.algorithm.base
 * @Description 
 * @author zq
 * @date 2021年3月24日 上午9:22:28
 * @Copyright 
 */

package com.zq.utils.algorithm.base;

/**
 * @Description 
 * @author zq
 * @date 2021年3月24日 上午9:22:28
 * @see
 * @since 2021年3月24日 上午9:22:28
 */
public class SinglyLinkedList {
    
    public SinglyLinkedList() {
    }
    
    private Node head;
    private Node tail;
    
    final static class Node {
        Node prev;
        Node next;
        
        Object data;
        public Node(Object o) {
            this.data = o;
            this.prev=this.next=null;
        }
    }
    
    public void add(Object o) {
        Node node=new Node(o);
        
        if(head==null){
            head = tail = node;
        }else {
            tail = tail.next = node;
            node.next=null;
        }
    }
    
    public void add1(Object o) {
        Node newNode = new Node(o);
        Node node = head;
        if(node ==null){
            head = newNode;
            return;
        }
        while(node.next!=null) {
            node=node.next;
        }
        node.next=newNode;
    }
    
    public void print() {
        Node node = head;
        while(node!=null) {
            node = node.next;
            System.out.println(1);
        }
    }
    
    public static void main(String[] args) {
         SinglyLinkedList list = new SinglyLinkedList();
         list.add1(1);
         list.add1(2);
         list.add1(3);
         list.print();
    }
}
