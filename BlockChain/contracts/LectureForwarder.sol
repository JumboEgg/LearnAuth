// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/metatx/ERC2771Forwarder.sol";

contract LectureForwarder is ERC2771Forwarder {
    constructor() ERC2771Forwarder("LectureForwarder") {}

    function name() public view returns (string memory) {
    return _EIP712Name();
}
}