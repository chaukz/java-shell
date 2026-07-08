import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Trie {

    private class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord;
        String fullWord;
    }

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.fullWord = word;
    }

    public boolean startsWith(String prefix) {
        TrieNode node = findNode(prefix);
        return node != null;
    }

    public List<String> getWordsWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = findNode(prefix);
        if (node != null) {
            collectWords(node, results);
        }
        return results;
    }

    private TrieNode findNode(String prefix) {
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private void collectWords(TrieNode node, List<String> results) {
        if (node.isEndOfWord) {
            results.add(node.fullWord);
        }
        for (TrieNode child : node.children.values()) {
            collectWords(child, results);
        }
    }
}
