package iorihuang.bankaccountmanager.assessments;

public class Assess20261024 {

    public static int isSameReflection(String word1, String word2) {
        // 1. Check for null; a null input cannot be a rotation
        if (word1 == null || word2 == null) {
            return -1;
        }

        // 2. If the lengths differ, they cannot be rotations of each other
        if (word1.length() != word2.length()) {
            return -1;
        }

        // 3. An empty string is a rotation of itself
        if (word1.length() == 0) {
            return 1;
        }

        // 4. Core logic: concatenate word1 with itself and check whether word2 is a substring
        // e.g. word1 = "sample" -> concatenated = "samplesample"
        // If word2 = "plesam", it is contained in "samplesample"
        String concatenated = word1 + word1;

        if (concatenated.contains(word2)) {
            return 1;
        } else {
            return -1;
        }
    }

    // main method for local testing (not needed for submission)
    public static void main(String[] args) {
        System.out.println(isSameReflection("sample", "plesam")); // expected: 1
        System.out.println(isSameReflection("sample", "sample")); // expected: 1
        System.out.println(isSameReflection("abc", "acb"));       // expected: -1
    }

    class Solution1 {
        /**
         Question

         A pizza shop makes serves both vegan pizza and meat pizza. Customers place N orders at the shop. Their order number is printed on their bill. The shop displays K out of N orders (both vegan and meat) on their display screen at any given time. The pizza shop is very famous and they receive many orders. To avoid confusion, vegan pizza orders are represented by positive numbers and meat pizza orders are represented by negative numbers on the screen. The orders are served in the order in which they are displayed on the screen. Each time an order is ready, its number is removed from the display screen and a new order is added to the display at the end of the list.

         A couple has arrived with their child Billy. Billy is a very naughty child. To keep him occupied, his parents tell him to make a list of the first meat pizza that appear in each set of K orders displayed on the shop's display screen.

         Write an algorithm to help Billy make a list of the first meat based pizza order numbers displayed on the screen each time an order is delivered to a customer.

         Input
         The first line of the input consists of an integer - orderPlaced_size, representing the total number of orders placed (N).
         The second line consists of N space-separated integers representing the vegan pizza and meat pizza order numbers of the orders placed by the customers.
         The last line consists of an integer - size, representing the number of orders displayed on the screen (K).

         Output
         Print space-separated integers representing the first meat pizza order of every K orders displayed on the screen each time an order is delivered to a customer. Print 0 if the screen does not display any meat pizza order.

         Constraints
         0
         ≤
         o
         r
         d
         e
         r
         P
         l
         a
         c
         e
         d
         _
         s
         i
         z
         e
         ≤
         10
         6
         0≤orderPlaced_size≤10
         6

         0
         ≤
         s
         i
         z
         e
         ≤
         o
         r
         d
         e
         r
         P
         l
         a
         c
         e
         d
         _
         s
         i
         z
         e
         0≤size≤orderPlaced_size
         −
         10
         9
         ≤
         −10
         9
         ≤ order numbers of the orders placed
         ≤
         10
         9
         ≤10
         9


         Example
         Input:
         6
         -11 -2 19 37 64 -18
         3

         Output:
         -11 -2 0 -18

         Explanation:
         Step 1: At the time of the first display, the order numbers displayed are [-11, -2, 19]. So, the first displayed meat pizza order number is -11.
         Step 2: At the time of the next display, the order numbers displayed are [-2, 19, 37]. So, the first displayed meat pizza order number is -2.
         Step 3: At the time of the next display, the order numbers displayed are [19, 37, 64]. Since no meat based pizza order is displayed, the output is 0.
         Step 4: Similarly, at the time of the next display, the order numbers displayed are [37, 64, -18]. So, the first displayed meat pizza order number is -18.
         So, the output is [-11, -2, 0, -18].

         问题

         一家披萨店同时供应纯素披萨和肉类披萨。顾客在店里下了 N 个订单。他们的订单号印在账单上。该店在任何给定时间在显示屏上显示 N 个订单中的 K 个（包括纯素和肉类）。这家披萨店非常出名，他们收到了很多订单。为了避免混淆，纯素披萨订单在屏幕上用正数表示，肉类披萨订单用负数表示。订单按照它们在屏幕上显示的顺序进行处理。每当一个订单准备好时，它的号码就会从显示屏上移除，并且一个新的订单会添加到列表的末尾。

         一对夫妇带着他们的孩子比利来了。比利是一个非常顽皮的孩子。为了让他有事可做，他的父母让他列出商店显示屏上每组 K 个订单中出现的第一个肉类披萨。

         编写一个算法来帮助比利，每当向顾客交付一个订单时，列出屏幕上显示的第一个肉类披萨订单号。

         输入
         输入的第一行包含一个整数 - orderPlaced_size，表示总订单数 (N)。
         第二行包含 N 个空格分隔的整数，表示顾客下的纯素披萨和肉类披萨订单号。
         最后一行包含一个整数 - size，表示屏幕上显示的订单数 (K)。

         输出
         打印以空格分隔的整数，表示每次向顾客交付订单时，屏幕上显示的 K 个订单中的第一个肉类披萨订单。如果屏幕没有显示任何肉类披萨订单，则打印 0。

         约束条件
         0
         ≤
         o
         r
         d
         e
         r
         P
         l
         a
         c
         e
         d
         _
         s
         i
         z
         e
         ≤
         10
         6
         0≤orderPlaced_size≤10
         6

         0
         ≤
         s
         i
         z
         e
         ≤
         o
         r
         d
         e
         r
         P
         l
         a
         c
         e
         d
         _
         s
         i
         z
         e
         0≤size≤orderPlaced_size
         −
         10
         9
         ≤
         −10
         9
         ≤ 订单号
         ≤
         10
         9
         ≤10
         9


         示例
         输入:
         6
         -11 -2 19 37 64 -18
         3

         输出:
         -11 -2 0 -18

         解释:
         步骤 1: 第一次显示时，显示的订单号是 [-11, -2, 19]。因此，显示的第一个肉类披萨订单号是 -11。
         步骤 2: 下一次显示时，显示的订单号是 [-2, 19, 37]。因此，显示的第一个肉类披萨订单号是 -2。
         步骤 3: 下一次显示时，显示的订单号是 [19, 37, 64]。因为没有显示肉类披萨订单，所以输出为 0。
         步骤 4: 同样，在下一次显示时，显示的订单号是 [37, 64, -18]。因此，显示的第一个肉类披萨订单号是 -18。
         所以，输出是 [-11, -2, 0, -18]。
         */
    }
}