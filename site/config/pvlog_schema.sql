SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `pvlog` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `pvlog` ;

-- -----------------------------------------------------
-- Table `pvlog`.`mach_snapshot_per`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_per` (
  `SNAPSHOT_PER` INT NOT NULL ,
  `SNAPSHOT_PER_NAM` VARCHAR(10) NOT NULL ,
  PRIMARY KEY (`SNAPSHOT_PER`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pvlog`.`mach_snapshot_retent`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_retent` (
  `SNAPSHOT_RETENT` INT NOT NULL ,
  `SNAPSHOT_RETENT_NM` VARCHAR(10) NOT NULL ,
  PRIMARY KEY (`SNAPSHOT_RETENT`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pvlog`.`MACH_SNAPSHOT_TYPE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_type` (
  `SNAPSHOT_TYPE_NM` VARCHAR(120) NOT NULL ,
  `SNAPSHOT_TYPE_DESC` VARCHAR(255) NULL ,
  `SNAPSHOT_PER` INT NULL ,
  `SNAPSHOT_RETENT` INT NULL ,
  `SVC_ID` VARCHAR(10) NULL ,
  INDEX `FK_SNAPSHOT_PER_idx` (`SNAPSHOT_PER` ASC) ,
  INDEX `FK_SNAPSHOT_RETENT_idx` (`SNAPSHOT_RETENT` ASC) ,
  INDEX `FK_SVC_ID_idx` (`SVC_ID` ASC) ,
  PRIMARY KEY (`SNAPSHOT_TYPE_NM`) ,
  CONSTRAINT `FK_SNAPSHOT_PER_a`
    FOREIGN KEY (`SNAPSHOT_PER` )
    REFERENCES `pvlog`.`mach_snapshot_per` (`SNAPSHOT_PER` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `FK_SNAPSHOT_RETENT_a`
    FOREIGN KEY (`SNAPSHOT_RETENT` )
    REFERENCES `pvlog`.`mach_snapshot_retent` (`SNAPSHOT_RETENT` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `pvlog`.`MACH_SNAPSHOT`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot` (
  `SNAPSHOT_ID` INT NOT NULL AUTO_INCREMENT ,
  `SNAPSHOT_DTE` TIMESTAMP NULL ,
  `SNAPSHOT_SCORE` INT NULL ,
  `SNAPSHOT_TYPE_NM` VARCHAR(120) NULL ,
  `CMNT` VARCHAR(2000) NULL ,
  PRIMARY KEY (`SNAPSHOT_ID`) ,
  INDEX `FK_SNAPSHOT_TYPE_NM_idx` (`SNAPSHOT_TYPE_NM` ASC) ,
  CONSTRAINT `FK_SNAPSHOT_TYPE_NM`
    FOREIGN KEY (`SNAPSHOT_TYPE_NM` )
    REFERENCES `pvlog`.`mach_snapshot_type` (`SNAPSHOT_TYPE_NM` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


/*-- -----------------------------------------------------
-- Table `pvlog`.`mach_snapshot_sgnl_perm_ret`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_sgnl_perm_ret` (
  `SNAPSHOT_ID` INT NOT NULL ,
  `SGNL_ID` VARCHAR(45) NOT NULL ,
  `SGNL_TIMESTP` TIMESTAMP NOT NULL ,
  `SGNL_VAL` VARCHAR(75) NOT NULL ,
  `SGNL_STAT` DECIMAL(4,0) NOT NULL ,
  `SGNL_SVRTY` DECIMAL(4,0) NOT NULL ,
  PRIMARY KEY (`SNAPSHOT_ID`,`SGNL_ID`) ,
  INDEX `FK_SNAPSHOT_ID_idx` (`SNAPSHOT_ID` ASC) ,
  INDEX `FK_SGNL_ID_idx` (`SGNL_ID` ASC) ,
  CONSTRAINT `FK_SNAPSHOT_ID`
    FOREIGN KEY (`SNAPSHOT_ID` )
    REFERENCES `pvlog`.`mach_snapshot` (`SNAPSHOT_ID` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB; */


-- -----------------------------------------------------
-- Table `pvlog`.`MACH_SNAPSHOT_TYPE_SGNL`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_type_sgnl` (
  `SNAPSHOT_TYPE_NM` VARCHAR(120) NOT NULL ,
  `SGNL_ID` VARCHAR(45) NOT NULL ,
  `ACTIVE_IND` CHAR(1)  NOT NULL DEFAULT 'Y',
  INDEX `FK_SGNL_ID_idx` (`SGNL_ID` ASC) ,
  INDEX `SNAPSHOT_TYPE_NM_idx` (`SNAPSHOT_TYPE_NM` ASC) ,
  PRIMARY KEY (`SNAPSHOT_TYPE_NM`,`SGNL_ID`) ,
  CONSTRAINT `FK_SNAPSHOT_TYPE_NM_a`
    FOREIGN KEY (`SNAPSHOT_TYPE_NM` )
    REFERENCES `pvlog`.`mach_snapshot_type` (`SNAPSHOT_TYPE_NM` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `pvlog`.`mach_snapshot_sgnl`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `pvlog`.`mach_snapshot_sgnl` (
  `SNAPSHOT_ID` INT NULL ,
  `SGNL_ID` VARCHAR(75) NULL ,
  `SGNL_TIMESTP` TIMESTAMP NOT NULL ,
  `SGNL_VAL` VARCHAR(75) NOT NULL ,
  `SGNL_STAT` DECIMAL(4,0) NOT NULL ,
  `SGNL_SVRTY` DECIMAL(4,0) NOT NULL ,
  CONSTRAINT `FK_SGNL_ID_a`
    FOREIGN KEY (`SGNL_ID` )
    REFERENCES `pvlog`.`mach_snapshot_type_sgnl` (`SGNL_ID` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
