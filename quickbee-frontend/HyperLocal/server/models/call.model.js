const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const callSchema = new Schema({
  caller: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  receiver: {
    type: Schema.Types.ObjectId,
    ref: 'User'
  },
  group: {
    type: Schema.Types.ObjectId,
    ref: 'Group'
  },
  callType: {
    type: String,
    enum: ['audio', 'video'],
    required: true
  },
  status: {
    type: String,
    enum: ['ringing', 'ongoing', 'ended', 'missed', 'declined'],
    default: 'ringing'
  },
  participants: [{
    user: {
      type: Schema.Types.ObjectId,
      ref: 'User'
    },
    joinedAt: {
      type: Date
    },
    leftAt: {
      type: Date
    },
    status: {
      type: String,
      enum: ['joined', 'left', 'declined'],
      default: 'joined'
    }
  }],
  startedAt: {
    type: Date
  },
  endedAt: {
    type: Date
  },
  duration: {
    type: Number, // in seconds
    default: 0
  },
  roomId: {
    type: String,
    required: true,
    unique: true
  }
}, {
  timestamps: true,
});

// Ensure either receiver or group is present
callSchema.pre('save', function(next) {
  if (!this.receiver && !this.group) {
    next(new Error('Call must have either a receiver or a group'));
  }
  next();
});

const Call = mongoose.model('Call', callSchema);
module.exports = Call;
