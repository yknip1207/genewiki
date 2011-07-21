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
-- Table structure for table `gene_product_ancestor`
--

DROP TABLE IF EXISTS `gene_product_ancestor`;
CREATE TABLE `gene_product_ancestor` (
  `gene_product_id` int(11) NOT NULL,
  `ancestor_id` int(11) NOT NULL,
  `phylotree_id` int(11) NOT NULL,
  `branch_length` float default NULL,
  `is_transitive` int(11) NOT NULL default '0',
  UNIQUE KEY `gene_product_id` (`gene_product_id`,`ancestor_id`,`phylotree_id`),
  KEY `ancestor_id` (`ancestor_id`),
  KEY `phylotree_id` (`phylotree_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
