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
    it("Should allow user to mint tokens via meta-transaction", async function () {
      const amount = ethers.parseEther("100"); // Mint 100 tokens (using parseEther for ether values)
      const nonce = await minimalForwarder.getNonce(owner.address);
  
      // Build forward request
      const data = catToken.interface.encodeFunctionData("mint", [owner.address, amount]);
      const forwardRequest = {
        from: owner.address,
        to: catToken.target,
        value: 0, // No ether sent with this meta-transaction
        gas: 1000000,
        nonce: nonce,
        data: data,
      };
  
      // Sign the forward request
      const domain = {
        name: "MinimalForwarder",
        version: "1",
        chainId: 31337, // Local network ID
        verifyingContract: minimalForwarder.address,
      };
      const types = {
        ForwardRequest: [
          { name: "from", type: "address" },
          { name: "to", type: "address" },
          { name: "value", type: "uint256" },
          { name: "gas", type: "uint256" },
          { name: "nonce", type: "uint256" },
          { name: "data", type: "bytes" },
        ],
      };
  
      const signature = await owner.signTypedData(domain, types, forwardRequest);
  
      // Execute the forward request via the MinimalForwarder
      const tx = await minimalForwarder.execute(forwardRequest, signature);
      await tx.wait();
  
      // Check if the minting was successful
      const balance = await catToken.balanceOf(owner.address);
      expect(balance).to.equal(amount);
    });
  
    it("Should fail if signature is invalid", async function () {
      const amount = ethers.parseEther("100"); // Mint 100 tokens (using parseEther for ether values)
      const nonce = await minimalForwarder.getNonce(student.address);
  
      // Build forward request
      const data = catToken.interface.encodeFunctionData("mint", [student.address, amount]);
      const forwardRequest = {
        from: student.address,
        to: catToken.target,
        value: 0, // No ether sent with this meta-transaction
        gas: 1000000,
        nonce: nonce,
        data: data,
      };
  
      // Sign the forward request with a different address
      const invalidSigner = lecturer;
      const domain = {
        name: "MinimalForwarder",
        version: "1",
        chainId: 31337, // Local network ID
        verifyingContract: minimalForwarder.address,
      };
      const types = {
        ForwardRequest: [
          { name: "from", type: "address" },
          { name: "to", type: "address" },
          { name: "value", type: "uint256" },
          { name: "gas", type: "uint256" },
          { name: "nonce", type: "uint256" },
          { name: "data", type: "bytes" },
        ],
      };
  
      const signature = await invalidSigner.signTypedData(domain, types, forwardRequest);
  
      // Try to execute the forward request with an invalid signature
      await expect(minimalForwarder.execute(forwardRequest, signature)).to.be.revertedWith(
        "MinimalForwarder: signature does not match request"
      );
    });
  
    it("Should execute a successful transfer via meta-transaction", async function () {
      const transferAmount = ethers.parseEther("50"); // Transfer 50 tokens
  
      // Mint tokens to the signer to transfer
      await catToken.mint(owner.address, ethers.parseEther("1000"));
  
      const nonce = await minimalForwarder.getNonce(owner.address);
  
      // Build forward request for token transfer
      const data = catToken.interface.encodeFunctionData("transfer", [student.address, transferAmount]);
      const forwardRequest = {
        from: owner.address,
        to: catToken.target,
        value: 0, // No ether sent with this meta-transaction
        gas: 1000000,
        nonce: nonce,
        data: data,
      };
  
      // Sign the forward request
      const domain = {
        name: "MinimalForwarder",
        version: "1",
        chainId: 31337, // Local network ID
        verifyingContract: minimalForwarder.address,
      };
      const types = {
        ForwardRequest: [
          { name: "from", type: "address" },
          { name: "to", type: "address" },
          { name: "value", type: "uint256" },
          { name: "gas", type: "uint256" },
          { name: "nonce", type: "uint256" },
          { name: "data", type: "bytes" },
        ],
      };
  
      const signature = await owner.signTypedData(domain, types, forwardRequest);
  
      // Execute the forward request via the MinimalForwarder
      const tx = await minimalForwarder.execute(forwardRequest, signature);
      await tx.wait();
  
      // Check balances after transfer
      const ownerBalance = await catToken.balanceOf(owner.address);
      const studentBalance = await catToken.balanceOf(student.address);
      expect(ownerBalance).to.equal(ethers.parseEther("950"));
      expect(studentBalance).to.equal(transferAmount);
    });
  });
});
