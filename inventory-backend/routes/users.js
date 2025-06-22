const express = require('express');
const router = express.Router();
const User = require('../models/User');
const bcrypt = require('bcrypt');

// Register new user
router.post('/register', async (req, res) => {
  try {
    const { username, password } = req.body; 

    // Check if username exists
    const existingUser = await User.findOne({ username });
    if (existingUser) {
      return res.status(400).json(false);
    }

    const newUser = new User({ username, password });
    await newUser.save();

    res.json(true);
  } catch (err) {
    console.error(err);
    res.status(500).json(false);
  }
});

// Login user
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    // Find user by username
    const user = await User.findOne({ username });
    if (!user) {
      return res.status(401).json(false);
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (isMatch) {
      return res.json(true);
    } else {
      return res.status(401).json(false);
    }
  } catch (err) {
    console.error(err);
    res.status(500).json(false);
  }
});

// Check if username is unique
router.get('/check-username', async (req, res) => {
  try {
    const { username } = req.query;
    const exists = await User.exists({ username });
    res.json(!exists);
  } catch (err) {
    console.error(err);
    res.status(500).json(false);
  }
});

// Test endpoint
router.get('/test', (req, res) => {
  res.send('Test passed');
});

module.exports = router;
