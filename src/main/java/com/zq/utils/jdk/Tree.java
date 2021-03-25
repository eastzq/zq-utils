/**  
 * @Title: Tree.java
 * @Package com.zq.utils.jdk
 * @Description 
 * @author zq
 * @date 2021年3月25日 下午7:01:54
 * @Copyright 
 */

package com.zq.utils.jdk;

import java.io.File;

/**
 * @Description
 * @author zq
 * @date 2021年3月25日 下午7:01:54
 * @see
 * @since 2021年3月25日 下午7:01:54
 * 
 *        二叉树 Binary tree 满二叉树 Full binary tree 完全二叉树 Complete binary tree
 *        二叉排序树 Binary sort tree 二叉搜索树 Binary search tree
 *        前序遍历 Preorder traversal 中序遍历 Inorder traversal
 *        后序遍历 Postorder traversal 哈夫曼树 Huffman tree 深度优先索引 Depth-First Search
 *        广度优先索引 Breath-First Search
 */

public class Tree {
    static class Node {
        String value = "";
        Node left;
        Node right;

        public Node(String value, Node left, Node right) {
            this.left = left;
            this.value = value;
            this.right = right;
        }

        public void print() {
            System.out.println(this.value);
        }
    }

    // 二叉树遍历
    // A
    // / \
    // B C
    // / \
    // D E
    public static void main(String[] args) {
        Node E = new Node("E", null, null);
        Node D = new Node("D", null, null);
        Node C = new Node("C", null, null);
        Node B = new Node("B", D, E);
        Node A = new Node("A", B, C);

        Tree tree = new Tree();
        tree.preorderTraversal(A);
        tree.inorderTraversal(A);
        tree.postorderTraversal(A);
    }

    public Tree() {

    }

    // 前序遍历
    // 主 左 右
    public void preorderTraversal(Node node) {
        if (node != null) {
            node.print();
            preorderTraversal(node.left);
            preorderTraversal(node.right);
        }
    }

    // 中序遍历
    // 左 主 右
    public void inorderTraversal(Node node) {
        if (node != null) {
            inorderTraversal(node.left);
            node.print();
            inorderTraversal(node.right);
        }

    }

    // 后续遍历
    // 左 右 主
    public void postorderTraversal(Node node) {
        if (node != null) {
            postorderTraversal(node.left);
            postorderTraversal(node.right);
            node.print();
        }
    }

    // 深度优先算法，遍历文件树为例。
    public void dfs(File dir) {
        System.out.println(dir.getName());
        File[] files = dir.listFiles();
        for (File file : files) {
            if (dir.isDirectory()) {
                dfs(file);
            } else {
                System.out.println(file.getName());
            }
        }
    }
    public void bfs(File dir) {
        
    }
}
