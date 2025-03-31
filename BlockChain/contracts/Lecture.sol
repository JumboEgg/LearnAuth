// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/metatx/ERC2771Context.sol";

/**
 * @title LectureSystem
 * @dev Contract for managing lectures, payments, and NFT certifications
 */
contract LectureSystem is ERC721URIStorage, AccessControl, ERC2771Context {
    // Roles
    bytes32 public constant ADMIN_ROLE = keccak256("ADMIN_ROLE");
    
    // Token
    IERC20 public catToken;
    
    // NFT token ID counter
    uint private _tokenIdCounter;
    
    // Structs
    struct Participant {
        uint16 participantId;
        uint8 settlementRatio; // Out of 100 (100%)
    }
    
    struct Lecture {
        string title;
        Participant[] participants;
        bool exists;
    }
    
    // Mappings
    mapping(uint16 => address) public users; // userId => userAddress
    mapping(uint16 => Lecture) public lectures; // lectureId => Lecture
    mapping(address => uint256[]) private userPurchases; // user address => lectureIds
    
    // Events
    // 이벤트 필터링에 사용할 변수에 indexed 키워드 추가
    // 수령자 id에만 indexed 적용
    event TokenWithdrawn(uint256 indexed userId, uint256 amount, string activityType);
    event TokenDeposited(uint256 indexed userId, uint256 amount, string activityType);
    event LectureCreated(uint256 lectureId, string title);
    event LecturePurchased(uint256 indexed userId, uint256 amount, string lectureTitle);
    event LectureSettled(uint16 indexed userId, uint16 indexed lectureId, uint256 amount, string lectureTitle);
    event NFTIssued(uint256 userId, uint256 tokenId);
    
    /**
     * @dev Constructor sets up the contract with initial roles and token address
     * @param _catToken Address of the CAT token contract
     * @param _trustedForwarder Address of the trusted forwarder for meta-transactions
     */
    constructor(address _catToken, address _trustedForwarder) 
        ERC721("Necessary Youth Achevement Certification", "NYA")
        ERC2771Context(_trustedForwarder) 
    {
        catToken = IERC20(_catToken);
        
        _grantRole(DEFAULT_ADMIN_ROLE, _msgSender());
        _grantRole(ADMIN_ROLE, _msgSender());
    }

    // ================ Token Management Functions ================
    
    /**
     * @dev Withdraw tokens from the user's balance
     * @param userId User identifier
     * @param amount Amount of tokens to withdraw
     */
    function withdrawToken(uint16 userId, uint256 amount) external {
        require(amount > 0, "Amount must be greater than zero");
        
        address sender = _msgSender();
        require(catToken.transferFrom(sender, address(this), amount), "Token transfer failed");
        
        emit TokenWithdrawn(userId, amount, "withdrawn");
    }
    
    /**
     * @dev Deposit tokens to the user's balance
     * @param userId User identifier
     * @param amount Amount of tokens to deposit
     */
    function depositToken(uint16 userId, uint256 amount) external {
        require(amount > 0, "Amount must be greater than zero");
        address sender = _msgSender();
        
        if (sender != users[userId]) {
            require(hasRole(ADMIN_ROLE, sender), "Only user or admin can deposit tokens");
        }
        
        require(catToken.transfer(users[userId], amount), "Token transfer failed");
        
        emit TokenDeposited(userId, amount, "deposit");
    }

    // ================ User Management Functions ===================
    /**
     * @dev 특정 주소가 보유한 CATToken의 개수를 확인
     * @param _address 토큰 개수를 확인할 address
     */
    function checkBalance(address _address) public view returns (uint256) {
        return catToken.balanceOf(_address);
    }

    /**
     * @dev 사용자 정보를 users에 추가
     * @param _userId DB 상의 userId
     * @param _userAddress user의 지갑 address
    */
    function addUser(uint16 _userId, address _userAddress) external {
        address sender = _msgSender();
        require(hasRole(ADMIN_ROLE, sender), "Only admin allowed");
        users[_userId] = _userAddress;
    }
    
    // ================ Lecture Management Functions ================
    
    /**
     * @dev Create a new lecture
     * @param lectureId Unique identifier for the lecture
     * @param title Title of the lecture
     * @param participants Array of lecture participants and their settlement ratios
     */
     // 저장할 강의 정보 넘기기
    function createLecture(
        uint16 lectureId, 
        string memory title,
        Participant[] memory participants
    ) external {
        address sender = _msgSender();
        require(hasRole(ADMIN_ROLE, sender) || !lectures[lectureId].exists, "Only admin or new lecture ID allowed");
        
        // Validate total settlement ratio equals 100 (100%)
        uint256 totalRatio = 0;
        for (uint i = 0; i < participants.length; i++) {
            totalRatio += participants[i].settlementRatio;
        }
        require(totalRatio == 100, "Total settlement ratio must be 100%");
        
        // Store the lecture
        Lecture storage lecture = lectures[lectureId];
        lecture.title = title;
        lecture.exists = true;
        
        // Clear previous participants if any
        delete lecture.participants;
        
        // Add participants
        for (uint i = 0; i < participants.length; i++) {
            lecture.participants.push(participants[i]);
        }
        
        emit LectureCreated(lectureId, title);
    }
    
    /**
     * @dev Purchase a lecture
     * @param userId User identifier
     * @param lectureId Identifier of the lecture to purchase
     * @param amount Amount of tokens to pay
     */
    function purchaseLecture(uint16 userId, uint16 lectureId, uint256 amount) external {
        require(lectures[lectureId].exists, "Lecture does not exist");
        require(amount > 0, "Amount must be greater than zero");
        
        address sender = _msgSender();
                
        Lecture storage lecture = lectures[lectureId];
        
        for (uint i = 0; i < lecture.participants.length; i++) {
            // Calculate amount based on settlement ratio
            uint256 participantAmount = (amount * lecture.participants[i].settlementRatio) / 100;
            
            // Transfer tokens from user to participant
            require(catToken.transferFrom(sender, users[lecture.participants[i].participantId], participantAmount), "Token transfer failed");
            
            emit LectureSettled(lecture.participants[i].participantId, lectureId, participantAmount, lecture.title);
        }
        
        // Record the purchase
        userPurchases[sender].push(lectureId);
        
        emit LecturePurchased(userId, amount, lectures[lectureId].title);
    }
    
    /**
     * @dev Get all lectures purchased by a user
     * @param userAddress Address of the user
     * @return Array of lecture IDs
     */
    function getUserPurchases(address userAddress) external view returns (uint256[] memory) {
        return userPurchases[userAddress];
    }
    
    // ================ NFT Functions ================
    
    /**
     * @dev Issue an NFT certificate to a user
     * @param userId NFT를 수령한 사용자
     * @param cid IPFS CID for the NFT metadata
     */
    function issueNFT(uint16 userId, string memory cid) external {
        address recipient = _msgSender();
        
        // Mint new NFT
        uint256 tokenId = _tokenIdCounter;
        _tokenIdCounter++;
        _safeMint(recipient, tokenId);
        
        // Set token URI
        string memory tokenURI = string(abi.encodePacked("ipfs://", cid));

        // private 저장 공간에 저장하기 때문에 front에서 cid와 IPFS key를 조합해 조회한다.
        // string memory tokenURI = string(abi.encodePacked(cid));
        _setTokenURI(tokenId, tokenURI);
        
        emit NFTIssued(userId, tokenId);
    }
    
    // ================ ERC2771Context Overrides ================
    
    function _msgSender() internal view override(Context, ERC2771Context) returns (address) {
        return ERC2771Context._msgSender();
    }
    
    function _msgData() internal view override(Context, ERC2771Context) returns (bytes calldata) {
        return ERC2771Context._msgData();
    }

    function _contextSuffixLength() internal view override(Context, ERC2771Context) returns (uint256) {
        return ERC2771Context._contextSuffixLength();
    }
    
    // ================ Required Overrides ================
    
    function supportsInterface(bytes4 interfaceId) public view override(ERC721URIStorage, AccessControl) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
}