package net.grappendorf.buyrightcommon;

public interface ProductListener {
  Products onSupplyProducts();

  void onProductSelect(Product product);

  void onProductEdit(long productId);

  void onProductSave(Product product);

  void onProductDelete(long product);
}
