const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("LectureSystem", function () {
  let MinimalForwarder;
  let CATToken;
  let LectureSystem;
  let minimalForwarder;
  let catToken;
  let lectureSystem;
  let owner;
  let admin;
  let lecturer;
  let lect;
  let student;
  let participant1;
  let participant2;
  let addrs;

  // Test constants
  const LECTURE_TITLE = "Introduction to Blockchain";
  const LECTURE_ID = 1;
  const USER_ID = 101;
  const STUDENT_ID = 102;
  const PARTICIPANT1_ID = 201;
  const PARTICIPANT2_ID = 202;
  const INITIAL_MINT = ethers.parseEther("10000");
  const PURCHASE_AMOUNT = ethers.parseEther("100");
  const NFT_CID = "QmT5NvUtoM5nWFfrQdVrFtvGfKFmG7AHE8P34isapyhCxX";

  beforeEach(async function () {
    // Deploy contracts and get signers
    [owner, admin, lecturer, lect, student, participant1, participant2, ...addrs] = await ethers.getSigners();

    // Deploy MinimalForwarder contract
    MinimalForwarder = await ethers.getContractFactory("MinimalForwarder");
    minimalForwarder = await MinimalForwarder.deploy();
    await minimalForwarder.waitForDeployment();

    // Deploy CATToken contract
    CATToken = await ethers.getContractFactory("CATToken");
    catToken = await CATToken.deploy(await minimalForwarder.getAddress());
    await catToken.waitForDeployment();

    // Deploy LectureSystem contract
    LectureSystem = await ethers.getContractFactory("LectureSystem");
    lectureSystem = await LectureSystem.deploy(
      await catToken.getAddress(),
      await minimalForwarder.getAddress()
    );
    await lectureSystem.waitForDeployment();

    // Mint tokens for testing
    await catToken.mint(owner.address, INITIAL_MINT);
    await catToken.mint(student.address, INITIAL_MINT);
    await catToken.mint(lecturer.address, INITIAL_MINT);

    // Add users to the system
    await lectureSystem.addUser(STUDENT_ID, student.address);
    await lectureSystem.addUser(PARTICIPANT1_ID, participant1.address);
    await lectureSystem.addUser(PARTICIPANT2_ID, participant2.address);

    // Approve tokens for the contract
    await catToken.approve(await lectureSystem.getAddress(), INITIAL_MINT);
    await catToken.connect(student).approve(await lectureSystem.getAddress(), INITIAL_MINT);
    await catToken.connect(lecturer).approve(await lectureSystem.getAddress(), INITIAL_MINT);
  });

  describe("Deployment", function () {
    it("Should set the right owner", async function () {
      expect(await lectureSystem.hasRole(await lectureSystem.DEFAULT_ADMIN_ROLE(), owner.address)).to.equal(true);
      expect(await lectureSystem.hasRole(await lectureSystem.ADMIN_ROLE(), owner.address)).to.equal(true);
    });

    it("Should set the right token", async function () {
      expect(await lectureSystem.catToken()).to.equal(await catToken.getAddress());
    });

    it("Should have the correct NFT name and symbol", async function () {
      expect(await lectureSystem.name()).to.equal("Necessary Youth Achevement Certification");
      expect(await lectureSystem.symbol()).to.equal("NYA");
    });
  });

  describe("User Management", function () {
    it("Should add a user correctly", async function () {
      const testUserId = 301;
      const testUserAddress = addrs[0].address;
      
      await lectureSystem.addUser(testUserId, testUserAddress);
      
      expect(await lectureSystem.users(testUserId)).to.equal(testUserAddress);
    });

    it("Should fail when non-admin tries to add a user", async function () {
      const testUserId = 302;
      const testUserAddress = addrs[1].address;
      
      await expect(
        lectureSystem.connect(student).addUser(testUserId, testUserAddress)
      ).to.be.revertedWith("Only admin allowed");
    });
  });

  describe("Token Operations", function () {
    it("Should withdraw tokens correctly", async function () {
      const withdrawAmount = ethers.parseEther("10");
      const initialContractBalance = await catToken.balanceOf(await lectureSystem.getAddress());
      const initialOwnerBalance = await catToken.balanceOf(owner.address);
      
      await expect(lectureSystem.withdrawToken(USER_ID, withdrawAmount))
        .to.emit(lectureSystem, "TokenWithdrawn")
        .withArgs(USER_ID, withdrawAmount, "withdrawn");
      
      const finalContractBalance = await catToken.balanceOf(await lectureSystem.getAddress());
      const finalOwnerBalance = await catToken.balanceOf(owner.address);
      
      expect(finalContractBalance).to.equal(initialContractBalance + withdrawAmount);
      expect(finalOwnerBalance).to.equal(initialOwnerBalance - withdrawAmount);
    });

    it("Should deposit tokens correctly", async function () {
      // First withdraw tokens to the contract
      const amount = ethers.parseEther("10");
      await lectureSystem.withdrawToken(STUDENT_ID, amount);
      
      const initialContractBalance = await catToken.balanceOf(await lectureSystem.getAddress());
      const initialStudentBalance = await catToken.balanceOf(student.address);
      
      await expect(lectureSystem.depositToken(STUDENT_ID, amount))
        .to.emit(lectureSystem, "TokenDeposited")
        .withArgs(STUDENT_ID, amount, "deposit");
      
      const finalContractBalance = await catToken.balanceOf(await lectureSystem.getAddress());
      const finalStudentBalance = await catToken.balanceOf(student.address);
      
      expect(finalContractBalance).to.equal(initialContractBalance - amount);
      expect(finalStudentBalance).to.equal(initialStudentBalance + amount);
    });

    it("Should check balances correctly", async function () {
      const balanceOwner = await lectureSystem.checkBalance(owner.address);
      expect(balanceOwner).to.equal(INITIAL_MINT);
      
      const balanceStudent = await lectureSystem.checkBalance(student.address);
      expect(balanceStudent).to.equal(INITIAL_MINT);
    });
  });

  describe("Lecture Management", function () {
    it("Should create a lecture correctly", async function () {
      const participants = [
        { participantId: PARTICIPANT1_ID, settlementRatio: 60 },
        { participantId: PARTICIPANT2_ID, settlementRatio: 40 }
      ];
      
      await expect(lectureSystem.createLecture(
        LECTURE_ID,
        LECTURE_TITLE,
        participants,
        lect.address
      ))
        .to.emit(lectureSystem, "LectureCreated")
        .withArgs(LECTURE_ID, LECTURE_TITLE, lect.address);
      
      const lecture = await lectureSystem.lectures(LECTURE_ID);
      expect(lecture.title).to.equal(LECTURE_TITLE);
      expect(lecture.lectureWallet).to.equal(lect.address);
      expect(lecture.exists).to.equal(true);
      
      // Check lecture role assignment
      expect(await lectureSystem.hasRole(await lectureSystem.LECTURE_ROLE(), lect.address)).to.equal(true);
    });

    it("Should fail when creating a lecture with incorrect settlement ratio", async function () {
      const invalidParticipants = [
        { participantId: PARTICIPANT1_ID, settlementRatio: 50 },
        { participantId: PARTICIPANT2_ID, settlementRatio: 30 }
      ];
      
      await expect(
        lectureSystem.createLecture(LECTURE_ID, LECTURE_TITLE, invalidParticipants, lecturer.address)
      ).to.be.revertedWith("Total settlement ratio must be 100%");
    });

    it("Should purchase a lecture correctly", async function () {
      // First create a lecture
      const participants = [
        { participantId: PARTICIPANT1_ID, settlementRatio: 60 },
        { participantId: PARTICIPANT2_ID, settlementRatio: 40 }
      ];
      
      await lectureSystem.createLecture(
        LECTURE_ID,
        LECTURE_TITLE,
        participants,
        lect.address
      );
      
      const initialLectBalance = await catToken.balanceOf(lect.address);
      const initialStudentBalance = await catToken.balanceOf(student.address);
      
      await expect(lectureSystem.connect(student).purchaseLecture(
        STUDENT_ID,
        LECTURE_ID,
        PURCHASE_AMOUNT
      ))
        .to.emit(lectureSystem, "LecturePurchased")
        .withArgs(STUDENT_ID, PURCHASE_AMOUNT, LECTURE_TITLE);
      
      const finalLectBalance = await catToken.balanceOf(lect.address);
      const finalStudentBalance = await catToken.balanceOf(student.address);
      
      expect(finalLectBalance).to.equal(initialLectBalance + PURCHASE_AMOUNT);
      expect(finalStudentBalance).to.equal(initialStudentBalance - PURCHASE_AMOUNT);
      
      // Check purchase record
      const purchases = await lectureSystem.getUserPurchases(student.address);
      expect(purchases.length).to.equal(1);
      expect(purchases[0]).to.equal(LECTURE_ID);
    });

    it("Should settle a lecture correctly", async function () {
      // First create a lecture
      const participants = [
        { participantId: PARTICIPANT1_ID, settlementRatio: 60 },
        { participantId: PARTICIPANT2_ID, settlementRatio: 40 }
      ];
      
      await lectureSystem.createLecture(
        LECTURE_ID,
        LECTURE_TITLE,
        participants,
        lect.address
      );
      
      // Purchase lecture
      await lectureSystem.connect(student).purchaseLecture(
        STUDENT_ID,
        LECTURE_ID,
        PURCHASE_AMOUNT
      );
      
      // Set up for lecture settlement
      const lectureSystemAddress = await lectureSystem.getAddress();
      
      // Important fix: The lecturer needs to approve the contract to spend tokens from their wallet
      // This was missing in the previous test
      await catToken.connect(lect).approve(lectureSystemAddress, PURCHASE_AMOUNT);
      
      const initialParticipant1Balance = await catToken.balanceOf(participant1.address);
      const initialParticipant2Balance = await catToken.balanceOf(participant2.address);
      const initialLectBalance = await catToken.balanceOf(lect.address);
      
      await expect(lectureSystem.connect(lect).settleLecture(LECTURE_ID))
        .to.emit(lectureSystem, "LectureSettled");
      
      const finalParticipant1Balance = await catToken.balanceOf(participant1.address);
      const finalParticipant2Balance = await catToken.balanceOf(participant2.address);
      const finalLectBalance = await catToken.balanceOf(lect.address);
      
      // Check balances after settlement
      expect(finalParticipant1Balance).to.equal(initialParticipant1Balance + PURCHASE_AMOUNT * BigInt(60) / BigInt(100));
      expect(finalParticipant2Balance).to.equal(initialParticipant2Balance + PURCHASE_AMOUNT * BigInt(40) / BigInt(100));
      expect(finalLectBalance).to.equal(initialLectBalance - PURCHASE_AMOUNT);
    });
  });

  describe("NFT Functions", function () {
    it("Should issue an NFT correctly", async function () {
      await expect(lectureSystem.connect(student).issueNFT(STUDENT_ID, NFT_CID))
        .to.emit(lectureSystem, "NFTIssued")
        .withArgs(STUDENT_ID, 0);
      
      expect(await lectureSystem.ownerOf(0)).to.equal(student.address);
      expect(await lectureSystem.tokenURI(0)).to.equal(NFT_CID);
    });
  });

  describe("Meta Transactions", function () {
    it("Should process a meta-transaction correctly", async function () {
      // First, create the forward request
      const forwarderAddress = await minimalForwarder.getAddress();
      const forwarder = minimalForwarder.connect(owner);
      
      const from = student.address;
      const to = await lectureSystem.getAddress();
      
      // Create participants for a lecture
      const participants = [
        { participantId: PARTICIPANT1_ID, settlementRatio: 60 },
        { participantId: PARTICIPANT2_ID, settlementRatio: 40 }
      ];
      
      // Create ABI-encoded function call for creating a lecture
      const LectureSystemInterface = new ethers.Interface([
        "function createLecture(uint16 lectureId, string memory title, tuple(uint16 participantId, uint8 settlementRatio)[] memory participants, address lectureWallet)"
      ]);
      
      const data = LectureSystemInterface.encodeFunctionData(
        "createLecture",
        [LECTURE_ID + 1, "Meta Transaction Lecture", participants, lecturer.address]
      );
      
      const nonce = await forwarder.getNonce(from);
      
      const req = {
        from: from,
        to: to,
        value: 0,
        gas: 1000000,
        nonce: nonce,
        data: data
      };
      
      // Sign the forward request - Fixed to use the proper signing method compatible with hardhat
      const domain = {
        name: "MinimalForwarder",
        version: "1",
        chainId: (await ethers.provider.getNetwork()).chainId,
        verifyingContract: forwarderAddress
      };
      
      const types = {
        ForwardRequest: [
          { name: "from", type: "address" },
          { name: "to", type: "address" },
          { name: "value", type: "uint256" },
          { name: "gas", type: "uint256" },
          { name: "nonce", type: "uint256" },
          { name: "data", type: "bytes" }
        ]
      };

      const reqForSigning = {
        from: req.from,
        to: req.to,
        value: req.value.toString(),
        gas: req.gas.toString(),
        nonce: req.nonce.toString(),
        data: req.data
      };

      const signature = await ethers.provider.send("eth_signTypedData_v4", [
        from,
        JSON.stringify({
          types,
          domain,
          primaryType: "ForwardRequest",
          message: reqForSigning
        })
      ]);
      
      // Execute the forward request
      await forwarder.execute(req, signature);
      
      // Verify lecture was created via meta-transaction
      const lecture = await lectureSystem.lectures(LECTURE_ID + 1);
      expect(lecture.title).to.equal("Meta Transaction Lecture");
      expect(lecture.lectureWallet).to.equal(lecturer.address);
      expect(lecture.exists).to.equal(true);
    });
  });
});
