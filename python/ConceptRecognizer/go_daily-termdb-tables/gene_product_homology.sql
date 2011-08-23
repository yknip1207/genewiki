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
-- Table structure for table `gene_product_homology`
--

DROP TABLE IF EXISTS `gene_product_homology`;
CREATE TABLE `gene_product_homology` (
  `gene_product1_id` int(11) NOT NULL,
  `gene_product2_id` int(11) NOT NULL,
  `relationship_type_id` int(11) NOT NULL,
  KEY `gene_product1_id` (`gene_product1_id`),
  KEY `gene_product2_id` (`gene_product2_id`),
  KEY `relationship_type_id` (`relationship_type_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
