const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("LectureSystem with LectureForwarder", function () {
  let lectureSystem, catToken, forwarder, 
      deployer, admin, user1, user2;
  
  // Sample data for testing
  const USER1_ID = 1001;
  const USER2_ID = 1002;
  const LECTURE_ID = 2001;
  const INITIAL_MINT = ethers.utils.parseUnits("1000", 18);
  const LECTURE_PRICE = ethers.utils.parseUnits("100", 18);

  before(async function () {
    // Get signers
    [deployer, admin, user1, user2] = await ethers.getSigners();
    
    // Deploy LectureForwarder
    const LectureForwarder = await ethers.getContractFactory("LectureForwarder");
    forwarder = await LectureForwarder.deploy();
    await forwarder.deployed();
    
    // Deploy CATToken
    const CATToken = await ethers.getContractFactory("CATToken");
    catToken = await CATToken.deploy(forwarder.address);
    await catToken.deployed();
    
    // Deploy LectureSystem
    const LectureSystem = await ethers.getContractFactory("LectureSystem");
    lectureSystem = await LectureSystem.deploy(catToken.address, forwarder.address);
    await lectureSystem.deployed();

    // Grant minter role to deployer
    await catToken.grantRole(await catToken.MINTER_ROLE(), deployer.address);
    
    // Mint tokens to users
    await catToken.mint(user1.address, INITIAL_MINT);
    await catToken.mint(user2.address, INITIAL_MINT);
    await catToken.mint(deployer.address, INITIAL_MINT);
    
    // Add users to the system
    await lectureSystem.addUser(USER1_ID, user1.address);
    await lectureSystem.addUser(USER2_ID, user2.address);
  });

  // Meta-transaction helper function (similar to the original test file)
  async function signMetaTxRequest(signer, forwarder, input) {
    const domainName = await forwarder.name();
    const chainId = (await signer.provider.getNetwork()).chainId;
    const domain = {
      name: domainName,
      version: "1",
      chainId,
      verifyingContract: forwarder.address,
    };

    const onChainNonce = await forwarder.nonces(input.from);

    const types = {
      ForwardRequest: [
        { name: "from",     type: "address" },
        { name: "to",       type: "address" },
        { name: "value",    type: "uint256" },
        { name: "gas",      type: "uint256" },
        { name: "nonce",    type: "uint256" },
        { name: "deadline", type: "uint48" },
        { name: "data",     type: "bytes" },
      ]
    };

    const typedRequest = {
      from: input.from,
      to: input.to,
      value: 0,
      gas: input.gas,
      nonce: onChainNonce,
      deadline: input.deadline,
      data: input.data,
    };

    const signature = await signer._signTypedData(domain, types, typedRequest);

    return {
      request: {
        from: typedRequest.from,
        to: typedRequest.to,
        value: typedRequest.value,
        gas: typedRequest.gas,
        deadline: typedRequest.deadline,
        data: typedRequest.data,
      },
      signature,
    };
  }

  // Tests for meta-transaction functionality
  describe("Meta-Transaction Tests", function () {
    it("Deployer can use forwarder to create lecture", async function () {
      const participants = [
        { participantId: USER1_ID, settlementRatio: 60 },
        { participantId: USER2_ID, settlementRatio: 40 }
      ];

      // Encode createLecture function call
      const data = lectureSystem.interface.encodeFunctionData("createLecture", [
        LECTURE_ID, 
        "Test Lecture", 
        participants
      ]);
      
      // Get current block timestamp
      const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
      const futureDeadline = blockTime + 1000;
      
      // Sign meta-transaction
      const { request, signature } = await signMetaTxRequest(
        deployer,
        forwarder,
        {
          from: deployer.address,
          to: lectureSystem.address,
          data,
          gas: 1_000_000,
          deadline: futureDeadline
        }
      );
      
      // Execute meta-transaction via a different user
      await forwarder.connect(user1).execute({
        from:      request.from,
        to:        request.to,
        value:     request.value,
        gas:       request.gas,
        deadline:  request.deadline,
        data:      data,
        signature: signature,
      });

      // Verify lecture was created
      const lecture = await lectureSystem.lectures(LECTURE_ID);
      expect(lecture.exists).to.be.true;
      expect(lecture.title).to.equal("Test Lecture");
    });

    it("User can use forwarder to purchase lecture", async function () {
      // Approve token spending for meta-transaction
      await catToken.connect(user1).approve(lectureSystem.address, LECTURE_PRICE);

      // Encode purchaseLecture function call
      const data = lectureSystem.interface.encodeFunctionData("purchaseLecture", [
        USER1_ID, 
        LECTURE_ID, 
        LECTURE_PRICE
      ]);
      
      // Get current block timestamp
      const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
      const futureDeadline = blockTime + 1000;
      
      // Sign meta-transaction
      const { request, signature } = await signMetaTxRequest(
        user1,
        forwarder,
        {
          from: user1.address,
          to: lectureSystem.address,
          data,
          gas: 1_000_000,
          deadline: futureDeadline
        }
      );
      
      // Execute meta-transaction via a different user
      await forwarder.connect(user2).execute({
        from:      request.from,
        to:        request.to,
        value:     request.value,
        gas:       request.gas,
        deadline:  request.deadline,
        data:      data,
        signature: signature,
      });

      // Verify lecture purchase
      const userPurchases = await lectureSystem.getUserPurchases(user1.address);
      expect(userPurchases[0]).to.equal(LECTURE_ID);
      
      const userToken = await lectureSystem.checkBalance(user1.address);
      console.log("user1 token is : " + userToken);
    });

    it("User can use forwarder to withdraw token", async function () {
      // Approve token spending for meta-transaction
      await catToken.connect(user1).approve(lectureSystem.address, LECTURE_PRICE);

      // Encode purchaseLecture function call
      const data = lectureSystem.interface.encodeFunctionData("withdrawToken", [
        USER1_ID, 
        LECTURE_PRICE
      ]);
      
      // Get current block timestamp
      const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
      const futureDeadline = blockTime + 1000;
      
      // Sign meta-transaction
      const { request, signature } = await signMetaTxRequest(
        user1,
        forwarder,
        {
          from: user1.address,
          to: lectureSystem.address,
          data,
          gas: 1_000_000,
          deadline: futureDeadline
        }
      );
      
      // Execute meta-transaction via a different user
      await forwarder.connect(user2).execute({
        from:      request.from,
        to:        request.to,
        value:     request.value,
        gas:       request.gas,
        deadline:  request.deadline,
        data:      data,
        signature: signature,
      });

      // Verify lecture purchase
      const userToken = await lectureSystem.checkBalance(user1.address);
      expect(userToken).to.equal(ethers.utils.parseUnits("860", 18));
    });
  });

  // Additional Direct Function Tests
  describe("Direct Function Tests", function () {
    it("Admin can add users", async function () {
      const newUser = await ethers.Wallet.createRandom();
      const newUserId = 9999;

      await lectureSystem.addUser(newUserId, newUser.address);
      const storedAddress = await lectureSystem.users(newUserId);
      expect(storedAddress).to.equal(newUser.address);
    });

    it("Can mint and check CAT token balance", async function () {
      const mintAmount = ethers.utils.parseUnits("500", 18);
      await catToken.mint(user1.address, mintAmount);
      
      const balance = await lectureSystem.checkBalance(user1.address);
      expect(balance).to.be.gt(INITIAL_MINT);
    });

    it("Can issue NFT", async function () {
      const cid = "QmT5vAYe6eHZ2QphFbfaWqk5W3vTeGrtL32cRx1zqPESkq";
    
      // Issue NFT to user1
      const tx = await lectureSystem.issueNFT(USER1_ID, cid);
      const receipt = await tx.wait();
      
      // Extract tokenId from the NFTIssued event
      const event = receipt.events.find(e => e.event === 'NFTIssued');
      const tokenId = event.args.tokenId;
      
      // Check that the user is the owner of the token
      const owner = await lectureSystem.getOwnerOfNFT(tokenId);
      expect(owner).to.equal(user1.address);
    
      // Check that the token URI is correct
      const tokenURI = await lectureSystem.getTokenURI(tokenId);
      expect(tokenURI).to.equal(`ipfs://${cid}`);
    })

    it("Can create lecture", async function () {
      const participants = [
        { participantId: USER1_ID, settlementRatio: 60 },
        { participantId: USER2_ID, settlementRatio: 40 }
      ];

      // Encode createLecture function call
      await lectureSystem.createLecture(
        LECTURE_ID, 
        "Test Lecture", 
        participants
      );
      const lecture = await lectureSystem.lectures(LECTURE_ID);
      expect(lecture.exists).to.be.true;
      expect(lecture.title).to.equal("Test Lecture");
    })

    it("Can deposit token to user", async function () {
      const mintAmount = ethers.utils.parseUnits("500", 18);
      await catToken.connect(deployer).approve(lectureSystem.address, mintAmount);
      
      await lectureSystem.depositToken(
        USER1_ID,
        mintAmount
      );

      const balance = await lectureSystem.checkBalance(user1.address);
      expect(balance).equal(ethers.utils.parseUnits("1860", 18))
    })
  });
});