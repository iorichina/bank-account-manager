package iorihuang.bankaccountmanager.assessments;

public class Assess20261024 {

    public static int isSameReflection(String word1, String word2) {
        // 1. 检查是否为空，如果都为空则视为相同
        if (word1 == null || word2 == null) {
            return -1;
        }

        // 2. 如果两个字符串长度不相等，绝对不可能是旋转字符串
        if (word1.length() != word2.length()) {
            return -1;
        }

        // 3. 如果长度为0，空字符串的旋转是它本身
        if (word1.length() == 0) {
            return 1;
        }

        // 4. 核心逻辑：将 word1 拼接自身，检查 word2 是否是拼接后字符串的子串
        // 例如 word1 = "sample" -> 拼接后 = "samplesample"
        // 如果 word2 = "plesam"，它包含在 "samplesample" 中
        String concatenated = word1 + word1;

        if (concatenated.contains(word2)) {
            return 1;
        } else {
            return -1;
        }
    }

    // 本地测试用的 main 方法（提交时不需要）
    public static void main(String[] args) {
        System.out.println(isSameReflection("sample", "plesam")); // 预期输出: 1
        System.out.println(isSameReflection("sample", "sample")); // 预期输出: 1
        System.out.println(isSameReflection("abc", "acb"));       // 预期输出: -1
    }
}