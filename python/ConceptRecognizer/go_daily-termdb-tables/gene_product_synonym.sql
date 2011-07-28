-- MySQL dump 10.11
--
-- Host: localhost    Database: go
-- ------------------------------------------------------
-- Server version	5.0.45
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE=',MYSQL40' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `gene_product_synonym`
--

DROP TABLE IF EXISTS `gene_product_synonym`;
CREATE TABLE `gene_product_synonym` (
  `gene_product_id` int(11) NOT NULL,
  `product_synonym` varchar(255) NOT NULL,
  UNIQUE KEY `gene_product_id` (`gene_product_id`,`product_synonym`),
  KEY `gs1` (`gene_product_id`),
  KEY `gs2` (`product_synonym`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
