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
-- Table structure for table `evidence_dbxref`
--

DROP TABLE IF EXISTS `evidence_dbxref`;
CREATE TABLE `evidence_dbxref` (
  `evidence_id` int(11) NOT NULL,
  `dbxref_id` int(11) NOT NULL,
  KEY `evx1` (`evidence_id`),
  KEY `evx2` (`dbxref_id`),
  KEY `evx3` (`evidence_id`,`dbxref_id`)
) TYPE=MyISAM;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-07-22  8:43:19
