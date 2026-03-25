-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 07, 2026 at 06:17 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `mookata_thestar`
--

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `categoriesID` int(11) NOT NULL,
  `categoriesName` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`categoriesID`, `categoriesName`) VALUES
(1, 'เนื้อสัตว์'),
(2, 'อาหารทะเล'),
(3, 'ของทานเล่น'),
(4, 'ผัก'),
(5, 'เส้น'),
(6, 'เครื่องดื่ม'),
(7, 'เซ็ต');

-- --------------------------------------------------------

--
-- Table structure for table `ordermenu`
--

CREATE TABLE `ordermenu` (
  `orderID` int(11) NOT NULL,
  `tableID` int(11) DEFAULT NULL,
  `orderDate` timestamp NOT NULL DEFAULT current_timestamp(),
  `orderStatus` varchar(20) DEFAULT NULL,
  `totalAmount` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ordermenu`
--

INSERT INTO `ordermenu` (`orderID`, `tableID`, `orderDate`, `orderStatus`, `totalAmount`) VALUES
(1, NULL, '2026-03-07 17:12:17', 'completed', 800.00);

-- --------------------------------------------------------

--
-- Table structure for table `order_detail`
--

CREATE TABLE `order_detail` (
  `detail_id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `unit_price` decimal(10,2) DEFAULT NULL,
  `item_status` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_detail`
--

INSERT INTO `order_detail` (`detail_id`, `order_id`, `product_id`, `quantity`, `unit_price`, `item_status`) VALUES
(1, 1, 32, 1, 199.00, 'served'),
(4, 1, 1, 2, 69.00, 'served'),
(5, 1, 28, 1, 10.00, 'served');

-- --------------------------------------------------------

--
-- Table structure for table `payment`
--

CREATE TABLE `payment` (
  `id` int(11) NOT NULL,
  `order_id` int(11) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

CREATE TABLE `product` (
  `product_id` int(11) NOT NULL,
  `categoriesID` int(11) DEFAULT NULL,
  `product_name` varchar(50) DEFAULT NULL,
  `product_detail` varchar(100) DEFAULT NULL,
  `product_price` decimal(10,2) DEFAULT NULL,
  `product_status` enum('available','unavailable') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`product_id`, `categoriesID`, `product_name`, `product_detail`, `product_price`, `product_status`) VALUES
(1, 1, 'Ska หมูออริจินัล', 'หมูหมักสูตรร้าน', 69.00, 'available'),
(2, 1, 'Ska Gold หมูทองคำ', 'หมูหมักสูตรพิเศษ', 69.00, 'available'),
(3, 1, 'Ska หมูหมาล่า', 'หมูหมักหม่าล่า', 69.00, 'available'),
(4, 1, 'Ska หมูพริกไทยดำ', 'หมูหมักพริกไทยดำ', 69.00, 'available'),
(5, 1, 'Ska หมูคลุกฝุ่น', 'หมูคลุกข้าวคั่ว', 69.00, 'available'),
(6, 1, 'หมูสามชั้น', 'หมูสามชั้นสด', 69.00, 'available'),
(7, 1, 'เบคอน', 'เบคอนสไลด์', 89.00, 'available'),
(8, 2, 'กุ้ง', 'กุ้งสด', 89.00, 'available'),
(9, 2, 'ปลาหมึก', 'ปลาหมึกสด', 89.00, 'available'),
(10, 3, 'ตับหมู', 'ตับหมูสด', 69.00, 'available'),
(11, 3, 'เต้าหู้ไข่', 'เต้าหู้ไข่', 20.00, 'available'),
(12, 3, 'มันหมู', 'มันหมูย่าง', 30.00, 'available'),
(13, 3, 'ไข่ไก่', 'ไข่สด', 10.00, 'available'),
(14, 3, 'ไข่ดองดิบ', 'ไข่ดอง', 20.00, 'available'),
(15, 3, 'ชีส', 'ชีสยืด', 49.00, 'available'),
(16, 3, 'ลูกชิ้นปลา', 'ลูกชิ้นปลา', 59.00, 'available'),
(17, 3, 'หมึกกรอบ', 'หมึกกรอบ', 69.00, 'available'),
(18, 4, 'ชุดผักรวม', 'ผักรวม', 69.00, 'available'),
(19, 4, 'ผักกาด', 'ผักกาดสด', 39.00, 'available'),
(20, 4, 'ผักบุ้ง', 'ผักบุ้งสด', 39.00, 'available'),
(21, 4, 'ผักกะหล่ำ', 'กะหล่ำปลี', 39.00, 'available'),
(22, 4, 'ข้าวโพดตัดเม็ด', 'ข้าวโพด', 39.00, 'available'),
(23, 4, 'เห็ดเข็มทอง', 'เห็ดเข็มทอง', 39.00, 'available'),
(24, 4, 'ตั้งโอ๋', 'ผักตั้งโอ๋', 39.00, 'available'),
(25, 5, 'ไวไว', 'บะหมี่กึ่งสำเร็จรูป', 20.00, 'available'),
(26, 5, 'วุ้นเส้น', 'วุ้นเส้น', 20.00, 'available'),
(27, 6, 'น้ำแข็ง', 'ถังน้ำแข็ง', 10.00, 'available'),
(28, 6, 'น้ำเปล่าขวดเล็ก', 'น้ำดื่ม', 10.00, 'available'),
(29, 6, 'น้ำเปล่าขวดใหญ่', 'น้ำดื่ม', 25.00, 'available'),
(30, 6, 'Pepsi ขวดเล็ก', 'Pepsi Original/Max', 20.00, 'available'),
(31, 6, 'Pepsi ขวดใหญ่', 'Pepsi Original/Max', 50.00, 'available'),
(32, 7, 'SKA Set หมูรวม', 'หมูออริจินัล หมูสามชั้น กุ้ง ปลาหมึก ตับหมู ชุดผักรวม', 199.00, 'available');

-- --------------------------------------------------------

--
-- Table structure for table `promotion`
--

CREATE TABLE `promotion` (
  `promotion_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `type` enum('percent','baht','add') NOT NULL,
  `value` decimal(10,2) DEFAULT NULL,
  `percent` decimal(10,2) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `free_product_id` int(11) DEFAULT NULL,
  `minspend` decimal(10,2) DEFAULT NULL,
  `req_product_id` int(11) DEFAULT NULL,
  `req_quantity` int(11) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotion`
--

INSERT INTO `promotion` (`promotion_id`, `name`, `detail`, `type`, `value`, `percent`, `quantity`, `free_product_id`, `minspend`, `req_product_id`, `req_quantity`, `start_date`, `end_date`) VALUES
(16, 'สั่งครบ 500 แถมน้ำ', 'ทานครบ 500 บาท ฟรี น้ำเปล่าขวดเล็ก 1 ขวด', 'add', NULL, NULL, 1, 28, 500.00, NULL, NULL, '2024-01-01 00:00:00', '2024-12-31 00:00:00'),
(17, 'Happy Monday', 'ส่วนลด 10% สำหรับยอดรวมเมื่อทานอาหารในวันจันทร์', 'percent', NULL, 10.00, NULL, NULL, NULL, NULL, NULL, '2024-01-01 00:00:00', '2024-12-31 00:00:00'),
(18, 'ลดชุดกินเอาตาย 100บ.', 'ส่วนลด 100 บาท เมื่อสั่งชุดกินเอาตาย', 'baht', 100.00, NULL, NULL, NULL, NULL, 32, 1, '2024-01-01 00:00:00', '2024-12-31 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `promotionusage`
--

CREATE TABLE `promotionusage` (
  `promo_id` int(11) NOT NULL,
  `od_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `promotionusage`
--

INSERT INTO `promotionusage` (`promo_id`, `od_id`) VALUES
(16, 4),
(17, 5),
(18, 1);

-- --------------------------------------------------------

--
-- Table structure for table `tables`
--

CREATE TABLE `tables` (
  `tableID` int(11) NOT NULL,
  `status` enum('available','occupied','reserved') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`categoriesID`);

--
-- Indexes for table `ordermenu`
--
ALTER TABLE `ordermenu`
  ADD PRIMARY KEY (`orderID`),
  ADD KEY `tableID` (`tableID`);

--
-- Indexes for table `order_detail`
--
ALTER TABLE `order_detail`
  ADD PRIMARY KEY (`detail_id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `payment`
--
ALTER TABLE `payment`
  ADD PRIMARY KEY (`id`),
  ADD KEY `order_id` (`order_id`);

--
-- Indexes for table `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `categoriesID` (`categoriesID`);

--
-- Indexes for table `promotion`
--
ALTER TABLE `promotion`
  ADD PRIMARY KEY (`promotion_id`),
  ADD KEY `free_product_id` (`free_product_id`),
  ADD KEY `req_product_id` (`req_product_id`);

--
-- Indexes for table `promotionusage`
--
ALTER TABLE `promotionusage`
  ADD PRIMARY KEY (`promo_id`,`od_id`),
  ADD KEY `od_id` (`od_id`);

--
-- Indexes for table `tables`
--
ALTER TABLE `tables`
  ADD PRIMARY KEY (`tableID`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `categoriesID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `ordermenu`
--
ALTER TABLE `ordermenu`
  MODIFY `orderID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `order_detail`
--
ALTER TABLE `order_detail`
  MODIFY `detail_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `payment`
--
ALTER TABLE `payment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `product`
--
ALTER TABLE `product`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `promotion`
--
ALTER TABLE `promotion`
  MODIFY `promotion_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `tables`
--
ALTER TABLE `tables`
  MODIFY `tableID` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ordermenu`
--
ALTER TABLE `ordermenu`
  ADD CONSTRAINT `ordermenu_ibfk_1` FOREIGN KEY (`tableID`) REFERENCES `tables` (`tableID`);

--
-- Constraints for table `order_detail`
--
ALTER TABLE `order_detail`
  ADD CONSTRAINT `order_detail_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `ordermenu` (`orderID`),
  ADD CONSTRAINT `order_detail_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`);

--
-- Constraints for table `payment`
--
ALTER TABLE `payment`
  ADD CONSTRAINT `payment_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `ordermenu` (`orderID`);

--
-- Constraints for table `product`
--
ALTER TABLE `product`
  ADD CONSTRAINT `product_ibfk_1` FOREIGN KEY (`categoriesID`) REFERENCES `categories` (`categoriesID`);

--
-- Constraints for table `promotion`
--
ALTER TABLE `promotion`
  ADD CONSTRAINT `promotion_ibfk_1` FOREIGN KEY (`free_product_id`) REFERENCES `product` (`product_id`),
  ADD CONSTRAINT `promotion_ibfk_2` FOREIGN KEY (`req_product_id`) REFERENCES `product` (`product_id`);

--
-- Constraints for table `promotionusage`
--
ALTER TABLE `promotionusage`
  ADD CONSTRAINT `promotionusage_ibfk_2` FOREIGN KEY (`od_id`) REFERENCES `order_detail` (`detail_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `promotionusage_ibfk_3` FOREIGN KEY (`promo_id`) REFERENCES `promotion` (`promotion_id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
