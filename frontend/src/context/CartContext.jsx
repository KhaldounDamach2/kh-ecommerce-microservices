import { createContext, useContext, useEffect, useReducer } from "react";

const CartContext = createContext(null);
const STORAGE_KEY = "ecommerce_cart";

const loadCart = () => {
  try {
    const storedCart = localStorage.getItem(STORAGE_KEY);
    if (!storedCart) return { items: [] };

    const parsedCart = JSON.parse(storedCart);
    if (!parsedCart || !Array.isArray(parsedCart.items)) return { items: [] };
    return { items: parsedCart.items };
  } catch {
    return { items: [] };
  }
};

const cartReducer = (state, action) => {
  switch (action.type) {
    case "ADD_ITEM": {
      const existingItem = state.items.find(
        (item) => item.productId === action.item.productId,
      );

      if (existingItem) {
        return {
          ...state,
          items: state.items.map((item) =>
            item.productId === action.item.productId
              ? { ...item, quantity: item.quantity + action.item.quantity }
              : item,
          ),
        };
      }

      return { ...state, items: [...state.items, action.item] };
    }
    case "REMOVE_ITEM":
      return {
        ...state,
        items: state.items.filter(
          (item) => item.productId !== action.productId,
        ),
      };
    case "UPDATE_QUANTITY":
      return {
        ...state,
        items:
          action.quantity <= 0
            ? state.items.filter((item) => item.productId !== action.productId)
            : state.items.map((item) =>
                item.productId === action.productId
                  ? { ...item, quantity: action.quantity }
                  : item,
              ),
      };
    case "CLEAR_CART":
      return { items: [] };
    default:
      return state;
  }
};

export const CartProvider = ({ children }) => {
  const [state, dispatch] = useReducer(cartReducer, undefined, loadCart);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  const addItem = (product, quantity = 1) => {
    dispatch({
      type: "ADD_ITEM",
      item: {
        productId: product.productId ?? product.id,
        name: product.name,
        imageUrl: product.imageUrl,
        unitPrice: product.unitPrice ?? product.price,
        sellerId: product.sellerId,
        quantity,
      },
    });
  };

  const removeItem = (productId) =>
    dispatch({ type: "REMOVE_ITEM", productId });
  const updateQuantity = (productId, quantity) =>
    dispatch({ type: "UPDATE_QUANTITY", productId, quantity });
  const clearCart = () => dispatch({ type: "CLEAR_CART" });

  const items = state.items;
  const totalItems = items.reduce((total, item) => total + item.quantity, 0);
  const totalPrice = Number(
    items
      .reduce((total, item) => total + item.unitPrice * item.quantity, 0)
      .toFixed(2),
  );

  const value = {
    items,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    totalItems,
    totalPrice,
    isEmpty: items.length === 0,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) throw new Error("useCart must be used within CartProvider");
  return context;
};
