package com.uex.fablab.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.uex.fablab.data.model.Product;
import com.uex.fablab.data.model.ProductType;
import com.uex.fablab.data.model.SubProduct;
import com.uex.fablab.data.services.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String products(HttpSession session, Model model) {
        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("USER_ADMIN"));
        List<Product> list = productService.findAll();
        model.addAttribute("products", list);
        model.addAttribute("isAdmin", isAdmin);
        return "products";
    }

    @GetMapping("/admin/add-product")
    public String addProductPage(Model model) {
        model.addAttribute("types", ProductType.values());
        return "admin/add-product";
    }

    @PostMapping("/admin/products")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("type") ProductType type,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3
    ) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setType(type);
        Product saved = productService.save(p);

        // Create default subproduct if price is provided
        if (price != null) {
            SubProduct sp = new SubProduct();
            sp.setProduct(saved);
            sp.setSubName("Estándar");
            sp.setPrice(price);
            sp.setStock(stock != null ? stock : 0);

            // Save first to get ID
            SubProduct savedSp = productService.saveSubProduct(sp);

            String img1 = saveImage(image1, saved.getId(), savedSp.getId(), 1);
            String img2 = saveImage(image2, saved.getId(), savedSp.getId(), 2);
            String img3 = saveImage(image3, saved.getId(), savedSp.getId(), 3);

            savedSp.setImage1(img1);
            savedSp.setImage2(img2);
            savedSp.setImage3(img3);

            productService.saveSubProduct(savedSp);
        }

        return "redirect:/admin/modify-product?id=" + saved.getId();
    }

    @PostMapping("/admin/subproducts/add")
    public String addSubProduct(
            @RequestParam("productId") Long productId,
            @RequestParam("subName") String subName,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3
    ) {
        Optional<Product> p = productService.findById(productId);
        if (p.isPresent()) {
            SubProduct sp = new SubProduct();
            sp.setProduct(p.get());
            sp.setSubName(subName);
            sp.setPrice(price);
            sp.setStock(stock);

            SubProduct savedSp = productService.saveSubProduct(sp);

            String img1 = saveImage(image1, productId, savedSp.getId(), 1);
            String img2 = saveImage(image2, productId, savedSp.getId(), 2);
            String img3 = saveImage(image3, productId, savedSp.getId(), 3);

            savedSp.setImage1(img1);
            savedSp.setImage2(img2);
            savedSp.setImage3(img3);

            productService.saveSubProduct(savedSp);
        }
        return "redirect:/admin/modify-product?id=" + productId;
    }

    @PostMapping("/admin/subproducts/delete")
    public String deleteSubProduct(@RequestParam("id") Long id) {
        Optional<SubProduct> sp = productService.findSubProductById(id);
        if (sp.isPresent()) {
            SubProduct sub = sp.get();
            deleteImage(sub.getImage1());
            deleteImage(sub.getImage2());
            deleteImage(sub.getImage3());

            Long productId = sub.getProduct().getId();
            productService.deleteSubProductById(id);
            return "redirect:/admin/modify-product?id=" + productId;
        }
        return "redirect:/admin/admin";
    }

    private void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        try {
            // imagePath is like "/img/upload/product_1/subproduct_1_1.jpg"
            String relativePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;

            String userDir = System.getProperty("user.dir");
            Path projectRoot = Paths.get(userDir);
            if (Files.exists(projectRoot.resolve("ProyectoMDAI"))) {
                projectRoot = projectRoot.resolve("ProyectoMDAI");
            }

            // Delete from src/main/resources/static/
            Path srcPath = projectRoot.resolve("src/main/resources/static/" + relativePath);
            Files.deleteIfExists(srcPath);

            // Delete from target/classes/static/
            Path targetPath = projectRoot.resolve("target/classes/static/" + relativePath);
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String saveImage(MultipartFile file, Long productId, Long subProductId, int imageNumber) {
        if (file == null || file.isEmpty()) return null;
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = "subproduct_" + subProductId + "_" + imageNumber + extension;
            String folderName = "product_" + productId;
            
            String userDir = System.getProperty("user.dir");
            Path projectRoot = Paths.get(userDir);
            if (Files.exists(projectRoot.resolve("ProyectoMDAI"))) {
                projectRoot = projectRoot.resolve("ProyectoMDAI");
            }
            
            // Save to src/main/resources/static/img/products/product_{id}/
            Path uploadPath = projectRoot.resolve("src/main/resources/static/img/products/").resolve(folderName);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.write(uploadPath.resolve(fileName), file.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            // Save to target/classes/static/img/products/product_{id}/
            Path targetPath = projectRoot.resolve("target/classes/static/img/products/").resolve(folderName);
            if (!Files.exists(targetPath)) Files.createDirectories(targetPath);
            Files.write(targetPath.resolve(fileName), file.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            return "/img/products/" + folderName + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/admin/modify-product")
    public String modifyProductPage(@RequestParam("id") Long id, Model model) {
        Optional<Product> opt = productService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/admin/admin";
        }
        model.addAttribute("product", opt.get());
        model.addAttribute("types", ProductType.values());
        model.addAttribute("subproducts", productService.findSubProductsByProductId(id));
        return "admin/modify-product";
    }

    @PostMapping("/admin/products/update")
    public String updateProduct(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("type") ProductType type
    ) {
        Optional<Product> opt = productService.findById(id);
        if (opt.isPresent()) {
            Product p = opt.get();
            p.setName(name);
            p.setDescription(description);
            p.setType(type);
            productService.save(p);
        }
        return "redirect:/admin/admin";
    }

    @PostMapping("/admin/products/delete")
    public String deleteProduct(@RequestParam("id") Long id) {
        deleteProductFolder(id);
        productService.deleteById(id);
        return "redirect:/admin/admin";
    }

    private void deleteProductFolder(Long productId) {
        try {
            String folderName = "product_" + productId;
            String userDir = System.getProperty("user.dir");
            Path projectRoot = Paths.get(userDir);
            if (Files.exists(projectRoot.resolve("ProyectoMDAI"))) {
                projectRoot = projectRoot.resolve("ProyectoMDAI");
            }

            // Delete from src
            Path srcPath = projectRoot.resolve("src/main/resources/static/img/products/").resolve(folderName);
            FileSystemUtils.deleteRecursively(srcPath);
            
            // Delete from target
            Path targetPath = projectRoot.resolve("target/classes/static/img/products/").resolve(folderName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/admin/subproducts/update")
    public String updateSubProduct(
            @RequestParam("id") Long id,
            @RequestParam("productId") Long productId,
            @RequestParam("subName") String subName,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam(value = "image1", required = false) MultipartFile image1,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3
    ) {
        Optional<SubProduct> opt = productService.findSubProductById(id);
        if (opt.isPresent()) {
            SubProduct sp = opt.get();
            sp.setSubName(subName);
            sp.setPrice(price);
            sp.setStock(stock);

            String img1 = saveImage(image1, productId, sp.getId(), 1);
            if (img1 != null) {
                if (sp.getImage1() != null && !sp.getImage1().equals(img1)) {
                    deleteImage(sp.getImage1());
                }
                sp.setImage1(img1);
            }

            String img2 = saveImage(image2, productId, sp.getId(), 2);
            if (img2 != null) {
                if (sp.getImage2() != null && !sp.getImage2().equals(img2)) {
                    deleteImage(sp.getImage2());
                }
                sp.setImage2(img2);
            }

            String img3 = saveImage(image3, productId, sp.getId(), 3);
            if (img3 != null) {
                if (sp.getImage3() != null && !sp.getImage3().equals(img3)) {
                    deleteImage(sp.getImage3());
                }
                sp.setImage3(img3);
            }

            productService.saveSubProduct(sp);
        }
        return "redirect:/admin/modify-product?id=" + productId;
    }
}
