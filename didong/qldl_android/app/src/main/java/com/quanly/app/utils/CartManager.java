package com.quanly.app.utils;

import com.quanly.app.model.CartItem;
import com.quanly.app.model.SanPham;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    public interface OnCartChangeListener {
        void onCartChanged(int totalCount, double totalAmount);
    }

    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final List<OnCartChangeListener> listeners = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addListener(OnCartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnCartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        int count = getTotalCount();
        double amount = getTotalAmount();
        for (OnCartChangeListener listener : listeners) {
            listener.onCartChanged(count, amount);
        }
    }

    public void addToCart(SanPham sp, int quantity) {
        if (sp == null || quantity <= 0) return;
        for (CartItem item : cartItems) {
            if (item.getSanPham() != null && item.getSanPham().getMaSP() != null &&
                    item.getSanPham().getMaSP().equalsIgnoreCase(sp.getMaSP())) {
                item.setSoLuong(item.getSoLuong() + quantity);
                notifyListeners();
                return;
            }
        }
        cartItems.add(new CartItem(sp, quantity));
        notifyListeners();
    }

    public void updateQuantity(String maSP, int newQuantity) {
        if (maSP == null) return;
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getSanPham() != null && maSP.equalsIgnoreCase(item.getSanPham().getMaSP())) {
                if (newQuantity <= 0) {
                    cartItems.remove(i);
                } else {
                    item.setSoLuong(newQuantity);
                }
                notifyListeners();
                return;
            }
        }
    }

    public void removeItem(String maSP) {
        updateQuantity(maSP, 0);
    }

    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getTotalCount() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getSoLuong();
        }
        return total;
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getThanhTien();
        }
        return total;
    }
}
