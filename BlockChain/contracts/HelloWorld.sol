// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/utils/Context.sol";
import "@openzeppelin/contracts/metatx/ERC2771Context.sol";

contract HelloWorld is ERC2771Context {
    string public message;
    address public lastCaller;
    
    event MessageUpdated(string newMessage, address caller);

    constructor(address trustedForwarder) ERC2771Context(trustedForwarder) {
        message = "Initial message";
    }
    
    function setMessage() external {
        message = "hello world";
        lastCaller = _msgSender(); // ERC2771Context에서 제공하는 _msgSender() 사용
        
        emit MessageUpdated(message, lastCaller);
    }
    
    function getMessage() external view returns (string memory) {
        return message;
    }
    
    function getLastCaller() external view returns (address) {
        return lastCaller;
    }
}