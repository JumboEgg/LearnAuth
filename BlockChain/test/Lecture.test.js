/*
const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("LectureSystem", function () {
  let lectureSystem;
  let catToken;
  let forwarder;
  let owner;
  let admin;
  let instructor1;
  let instructor2;
  let student1;
  let student2;
  
  // Constants for testing
  const ADMIN_ROLE = ethers.utils.keccak256(ethers.utils.toUtf8Bytes("ADMIN_ROLE"));
  const DEFAULT_ADMIN_ROLE = '0x0000000000000000000000000000000000000000000000000000000000000000';
  
  beforeEach(async function () {
    // Get signers
    [owner, admin, instructor1, instructor2, student1, student2] = await ethers.getSigners();
    
    // Deploy Forwarder contract
    const ForwarderFactory = await ethers.getContractFactory("LectureForwarder");
    forwarder = await ForwarderFactory.deploy();
    await forwarder.deployed();
    
    // Deploy MockERC20 token (CAT Token)
    const CATTokenFactory = await ethers.getContractFactory("CATToken");
    catToken = await CATTokenFactory.deploy(forwarder.address);
    await catToken.deployed();
    
    // Mint tokens to test accounts
    await catToken.mint(student1.address, ethers.utils.parseEther("1000"));
    await catToken.mint(student2.address, ethers.utils.parseEther("1000"));
    
    // Deploy LectureSystem contract
    const LectureSystemFactory = await ethers.getContractFactory("LectureSystem");
    lectureSystem = await LectureSystemFactory.deploy(catToken.address, forwarder.address);
    await lectureSystem.deployed();
    
    // Grant ADMIN_ROLE to admin
    await lectureSystem.grantRole(ADMIN_ROLE, admin.address);
    
    // Register users
    await lectureSystem.connect(admin).addUser(1, instructor1.address); // instructor1 with ID 1
    await lectureSystem.connect(admin).addUser(2, instructor2.address); // instructor2 with ID 2
    await lectureSystem.connect(admin).addUser(3, student1.address);    // student1 with ID 3
    await lectureSystem.connect(admin).addUser(4, student2.address);    // student2 with ID 4
  });
  
  describe("Contract Initialization", function () {
    it("Should set the correct token address", async function () {
      expect(await lectureSystem.catToken()).to.equal(catToken.address);
    });
    
    it("Should set the correct roles", async function () {
      expect(await lectureSystem.hasRole(DEFAULT_ADMIN_ROLE, owner.address)).to.be.true;
      expect(await lectureSystem.hasRole(ADMIN_ROLE, owner.address)).to.be.true;
      expect(await lectureSystem.hasRole(ADMIN_ROLE, admin.address)).to.be.true;
    });
  });
  
  describe("User Management", function () {
    it("Should register users correctly", async function () {
      expect(await lectureSystem.users(1)).to.equal(instructor1.address);
      expect(await lectureSystem.users(2)).to.equal(instructor2.address);
      expect(await lectureSystem.users(3)).to.equal(student1.address);
      expect(await lectureSystem.users(4)).to.equal(student2.address);
    });
    
    it("Should not allow non-admins to register users", async function () {
      await expect(
        lectureSystem.connect(student1).addUser(5, ethers.constants.AddressZero)
      ).to.be.revertedWith("Only admin allowed");
    });
    
    it("Should correctly check user balances", async function () {
      const balance = await lectureSystem.checkBalance(student1.address);
      expect(balance).to.equal(ethers.utils.parseEther("1000"));
    });
  });
  
  describe("Token Management", function () {
    beforeEach(async function () {
      // Approve LectureSystem contract to spend student tokens
      await catToken.connect(student1).approve(lectureSystem.address, ethers.utils.parseEther("500"));
    });
    
    it("Should allow withdrawal of tokens", async function () {
      const withdrawAmount = ethers.utils.parseEther("100");
      
      await expect(
        lectureSystem.connect(student1).withdrawToken(3, withdrawAmount)
      ).to.emit(lectureSystem, "TokenWithdrawn")
        .withArgs(3, withdrawAmount, "withdrawn");
      
      // Check contract balance increased
      expect(await catToken.balanceOf(lectureSystem.address)).to.equal(withdrawAmount);
      // Check student balance decreased
      expect(await catToken.balanceOf(student1.address)).to.equal(ethers.utils.parseEther("900"));
    });
    
    it("Should allow deposit of tokens", async function () {
      // First withdraw some tokens to the contract
      const withdrawAmount = ethers.utils.parseEther("100");
      await lectureSystem.connect(student1).withdrawToken(3, withdrawAmount);
      
      // Then deposit back
      const depositAmount = ethers.utils.parseEther("50");
      
      await expect(
        lectureSystem.connect(admin).depositToken(3, depositAmount)
      ).to.emit(lectureSystem, "TokenDeposited")
        .withArgs(3, depositAmount, "deposit");
      
      // Check balances
      expect(await catToken.balanceOf(lectureSystem.address)).to.equal(ethers.utils.parseEther("50"));
      expect(await catToken.balanceOf(student1.address)).to.equal(ethers.utils.parseEther("950"));
    });
    
    it("Should revert on zero amount", async function () {
      await expect(
        lectureSystem.connect(student1).withdrawToken(3, 0)
      ).to.be.revertedWith("Amount must be greater than zero");
      
      await expect(
        lectureSystem.connect(admin).depositToken(3, 0)
      ).to.be.revertedWith("Amount must be greater than zero");
    });
  });
  
  describe("Lecture Management", function () {
    it("Should create a lecture correctly", async function () {
      const lectureId = 1;
      const title = "Blockchain Basics";
      const participants = [
        { participantId: 1, settlementRatio: 60 }, // instructor1 gets 60%
        { participantId: 2, settlementRatio: 40 }  // instructor2 gets 40%
      ];
      
      await expect(
        lectureSystem.connect(admin).createLecture(lectureId, title, participants)
      ).to.emit(lectureSystem, "LectureCreated")
        .withArgs(lectureId, title);
      
      // Check lecture was created
      const lecture = await lectureSystem.lectures(lectureId);
      expect(lecture.title).to.equal(title);
      expect(lecture.exists).to.be.true;
    });
    
    it("Should validate settlement ratio totals 100%", async function () {
      const lectureId = 1;
      const title = "Invalid Lecture";
      const invalidParticipants = [
        { participantId: 1, settlementRatio: 50 }, // Only 90% total
        { participantId: 2, settlementRatio: 40 }
      ];
      
      await expect(
        lectureSystem.connect(admin).createLecture(lectureId, title, invalidParticipants)
      ).to.be.revertedWith("Total settlement ratio must be 100%");
    });
    
    it("Should purchase a lecture and settle payments correctly", async function () {
      // Create a lecture first
      const lectureId = 1;
      const title = "Solidity Programming";
      const participants = [
        { participantId: 1, settlementRatio: 60 }, // instructor1 gets 60%
        { participantId: 2, settlementRatio: 40 }  // instructor2 gets 40%
      ];
      
      await lectureSystem.connect(admin).createLecture(lectureId, title, participants);
      
      // Approve spending
      const purchaseAmount = ethers.utils.parseEther("100");
      await catToken.connect(student1).approve(lectureSystem.address, purchaseAmount);
      
      // Purchase the lecture
      await expect(
        lectureSystem.connect(student1).purchaseLecture(3, lectureId, purchaseAmount)
      ).to.emit(lectureSystem, "LecturePurchased")
        .withArgs(3, purchaseAmount, title);
      
      // Check settlements
      expect(await catToken.balanceOf(instructor1.address))
        .to.equal(ethers.utils.parseEther("60")); // 60% of 100
      
      expect(await catToken.balanceOf(instructor2.address))
        .to.equal(ethers.utils.parseEther("40")); // 40% of 100
      
      // Check user purchases
      const purchases = await lectureSystem.getUserPurchases(student1.address);
      expect(purchases.length).to.equal(1);
      expect(purchases[0]).to.equal(lectureId);
    });
    
    it("Should revert when purchasing non-existent lecture", async function () {
      const nonExistentLectureId = 999;
      const purchaseAmount = ethers.utils.parseEther("100");
      
      await catToken.connect(student1).approve(lectureSystem.address, purchaseAmount);
      
      await expect(
        lectureSystem.connect(student1).purchaseLecture(3, nonExistentLectureId, purchaseAmount)
      ).to.be.revertedWith("Lecture does not exist");
    });
  });
  
  describe("NFT Management", function () {
    it("Should issue an NFT certificate", async function () {
      const userId = 3; // student1
      const cid = "QmHash123456"; // Mock IPFS CID
      
      await expect(
        lectureSystem.connect(student1).issueNFT(userId, cid)
      ).to.emit(lectureSystem, "NFTIssued")
        .withArgs(userId, 0); // First token has ID 0
      
      // Check NFT ownership
      expect(await lectureSystem.ownerOf(0)).to.equal(student1.address);
      
      // Check token URI
      expect(await lectureSystem.tokenURI(0)).to.equal(`ipfs://${cid}`);
    });
    
    it("Should mint multiple NFTs with increasing token IDs", async function () {
      // First NFT
      await lectureSystem.connect(student1).issueNFT(3, "Qm1");
      
      // Second NFT
      await lectureSystem.connect(student2).issueNFT(4, "Qm2");
      
      // Check ownership
      expect(await lectureSystem.ownerOf(0)).to.equal(student1.address);
      expect(await lectureSystem.ownerOf(1)).to.equal(student2.address);
      
      // Check URIs
      expect(await lectureSystem.tokenURI(0)).to.equal("ipfs://Qm1");
      expect(await lectureSystem.tokenURI(1)).to.equal("ipfs://Qm2");
    });
  });
  
  // 추가로 메타트랜잭션을 테스트하려면 더 복잡한 설정이 필요합니다
  // 여기서는 기본 기능들만 테스트합니다
});
*/