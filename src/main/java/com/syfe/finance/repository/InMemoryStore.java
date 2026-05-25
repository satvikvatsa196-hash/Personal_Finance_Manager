package com.syfe.finance.repository;

import com.syfe.finance.model.AppUser;
import com.syfe.finance.model.Category;
import com.syfe.finance.model.CategoryType;
import com.syfe.finance.model.SavingsGoal;
import com.syfe.finance.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryStore {
    private static final List<Category> DEFAULT_CATEGORIES = List.of(
            new Category("Salary", CategoryType.INCOME, false, null),
            new Category("Food", CategoryType.EXPENSE, false, null),
            new Category("Rent", CategoryType.EXPENSE, false, null),
            new Category("Transportation", CategoryType.EXPENSE, false, null),
            new Category("Entertainment", CategoryType.EXPENSE, false, null),
            new Category("Healthcare", CategoryType.EXPENSE, false, null),
            new Category("Utilities", CategoryType.EXPENSE, false, null)
    );

    private final AtomicLong userIds = new AtomicLong(1);
    private final AtomicLong transactionIds = new AtomicLong(1);
    private final AtomicLong goalIds = new AtomicLong(1);
    private final ConcurrentMap<String, AppUser> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Category> customCategories = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Transaction> transactions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, SavingsGoal> goals = new ConcurrentHashMap<>();

    public AppUser createUser(String username, String passwordHash, String fullName, String phoneNumber) {
        AppUser user = new AppUser(userIds.getAndIncrement(), username.toLowerCase(Locale.ROOT), passwordHash, fullName, phoneNumber);
        AppUser existing = users.putIfAbsent(user.username(), user);
        return existing == null ? user : null;
    }

    public Optional<AppUser> findUser(String username) {
        return Optional.ofNullable(users.get(username.toLowerCase(Locale.ROOT)));
    }

    public List<Category> categoriesFor(String username) {
        List<Category> categories = new ArrayList<>(DEFAULT_CATEGORIES);
        customCategories.values().stream()
                .filter(category -> username.equals(category.ownerUsername()))
                .sorted(Comparator.comparing(Category::name))
                .forEach(categories::add);
        return categories;
    }

    public Optional<Category> findCategory(String username, String name) {
        return categoriesFor(username).stream()
                .filter(category -> category.name().equals(name))
                .findFirst();
    }

    public boolean categoryNameExists(String username, String name) {
        return categoriesFor(username).stream().anyMatch(category -> category.name().equalsIgnoreCase(name));
    }

    public Category saveCategory(String username, String name, CategoryType type) {
        Category category = new Category(name, type, true, username);
        customCategories.put(categoryKey(username, name), category);
        return category;
    }

    public Optional<Category> findCustomCategory(String username, String name) {
        return Optional.ofNullable(customCategories.get(categoryKey(username, name)));
    }

    public void deleteCustomCategory(String username, String name) {
        customCategories.remove(categoryKey(username, name));
    }

    public Transaction saveTransaction(Transaction transaction) {
        Transaction saved = transaction.id() == 0
                ? new Transaction(transactionIds.getAndIncrement(), transaction.ownerUsername(), transaction.amount(), transaction.date(), transaction.category(), transaction.description(), transaction.type())
                : transaction;
        transactions.put(saved.id(), saved);
        return saved;
    }

    public Optional<Transaction> findTransaction(long id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public List<Transaction> transactionsFor(String username) {
        return transactions.values().stream()
                .filter(transaction -> username.equals(transaction.ownerUsername()))
                .sorted(Comparator.comparing(Transaction::date).reversed().thenComparing(Comparator.comparing(Transaction::id).reversed()))
                .toList();
    }

    public boolean categoryInUse(String username, String categoryName) {
        return transactions.values().stream()
                .anyMatch(transaction -> username.equals(transaction.ownerUsername()) && transaction.category().equals(categoryName));
    }

    public void deleteTransaction(long id) {
        transactions.remove(id);
    }

    public SavingsGoal saveGoal(SavingsGoal goal) {
        SavingsGoal saved = goal.id() == 0
                ? new SavingsGoal(goalIds.getAndIncrement(), goal.ownerUsername(), goal.goalName(), goal.targetAmount(), goal.targetDate(), goal.startDate())
                : goal;
        goals.put(saved.id(), saved);
        return saved;
    }

    public Optional<SavingsGoal> findGoal(long id) {
        return Optional.ofNullable(goals.get(id));
    }

    public List<SavingsGoal> goalsFor(String username) {
        return goals.values().stream()
                .filter(goal -> username.equals(goal.ownerUsername()))
                .sorted(Comparator.comparing(SavingsGoal::id))
                .toList();
    }

    public void deleteGoal(long id) {
        goals.remove(id);
    }

    private String categoryKey(String username, String name) {
        return username + "::" + name.toLowerCase(Locale.ROOT);
    }
}
