require('dotenv').config();

const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const http = require('http');
const { Server } = require("socket.io");

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: ["http://localhost:5173", "https://societyconnect.vercel.app"],
    methods: ["GET", "POST","PUT", "DELETE"]
  }
});

const port = process.env.PORT || 5001;

app.use(cors());
app.use(express.json());

const uri = process.env.MONGO_URI;
mongoose.connect(uri);
const connection = mongoose.connection;
connection.once('open', async () => {
  console.log("✅ MongoDB database connection established successfully");
  // Seed categories on startup
  const seedCategories = require('./seedCategories');
  await seedCategories();
});

let userSocketMap = new Map();
let typingUsers = new Map(); // Track typing users
app.set('socketio', io);
app.set('userSocketMap', userSocketMap);
app.set('typingUsers', typingUsers);

// API Routes
const postsRouter = require('./routes/posts');
const usersRouter = require('./routes/users');
const notificationsRouter = require('./routes/notifications');
const categoriesRouter = require('./routes/categories');
const messagesRouter = require('./routes/messages');

app.use('/api/posts', postsRouter);
app.use('/api/users', usersRouter);
app.use('/api/notifications', notificationsRouter);
app.use('/api/categories', categoriesRouter);
app.use('/api/messages', messagesRouter);

// Socket.io connection logic
io.on('connection', (socket) => {
  console.log(`a user connected: ${socket.id}`);
  socket.on('addUser', (userId) => {
    userSocketMap.set(userId, socket.id);
    console.log(`User ${userId} added with socket ID ${socket.id}`);
  });

  // Handle sending messages
  socket.on('sendMessage', async (data) => {
    try {
      const { receiverId, content, senderId } = data;
      const receiverSocketId = userSocketMap.get(receiverId);

      // Emit to receiver if online
      if (receiverSocketId) {
        io.to(receiverSocketId).emit('receiveMessage', {
          sender: senderId,
          receiver: receiverId,
          content,
          timestamp: new Date()
        });
      }
    } catch (err) {
      console.error('Error sending message:', err);
    }
  });

  // Typing indicators
  socket.on('typing', (data) => {
    const { senderId, receiverId, isTyping } = data;
    const receiverSocketId = userSocketMap.get(receiverId);

    if (receiverSocketId) {
      io.to(receiverSocketId).emit('userTyping', {
        senderId,
        isTyping
      });
    }
  });

  // Presence updates
  socket.on('updatePresence', async (data) => {
    try {
      const { userId, isOnline, status } = data;
      const User = require('./models/user.model');

      await User.findByIdAndUpdate(userId, {
        isOnline,
        status,
        lastSeen: new Date(),
        socketId: socket.id
      });

      // Broadcast presence update to all connected users
      socket.broadcast.emit('presenceUpdate', {
        userId,
        isOnline,
        status,
        lastSeen: new Date()
      });
    } catch (err) {
      console.error('Error updating presence:', err);
    }
  });

  // Group messaging
  socket.on('joinGroup', (groupId) => {
    socket.join(`group_${groupId}`);
    console.log(`User joined group ${groupId}`);
  });

  socket.on('leaveGroup', (groupId) => {
    socket.leave(`group_${groupId}`);
    console.log(`User left group ${groupId}`);
  });

  socket.on('sendGroupMessage', async (data) => {
    try {
      const { groupId, content, senderId } = data;

      // Emit to all group members
      io.to(`group_${groupId}`).emit('receiveGroupMessage', {
        groupId,
        sender: senderId,
        content,
        timestamp: new Date()
      });
    } catch (err) {
      console.error('Error sending group message:', err);
    }
  });

  // WebRTC signaling for calls
  socket.on('callUser', (data) => {
    const { callerId, receiverId, signalData, callType } = data;
    const receiverSocketId = userSocketMap.get(receiverId);

    if (receiverSocketId) {
      io.to(receiverSocketId).emit('incomingCall', {
        callerId,
        signalData,
        callType
      });
    }
  });

  socket.on('answerCall', (data) => {
    const { callerId, signalData } = data;
    const callerSocketId = userSocketMap.get(callerId);

    if (callerSocketId) {
      io.to(callerSocketId).emit('callAnswered', { signalData });
    }
  });

  socket.on('endCall', (data) => {
    const { receiverId } = data;
    const receiverSocketId = userSocketMap.get(receiverId);

    if (receiverSocketId) {
      io.to(receiverSocketId).emit('callEnded');
    }
  });

  socket.on('disconnect', () => {
    for (let [userId, socketId] of userSocketMap.entries()) {
      if (socketId === socket.id) {
        userSocketMap.delete(userId);
        // Update user presence to offline
        const User = require('./models/user.model');
        User.findByIdAndUpdate(userId, {
          isOnline: false,
          lastSeen: new Date()
        }).catch(err => console.error('Error updating offline status:', err));

        // Broadcast offline status
        socket.broadcast.emit('presenceUpdate', {
          userId,
          isOnline: false,
          lastSeen: new Date()
        });
        break;
      }
    }
    console.log('user disconnected');
  });
});

server.listen(port, () => {
  console.log(`🚀 Server is running on port: ${port}`);
});
