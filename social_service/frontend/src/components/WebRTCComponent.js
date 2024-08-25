import React, { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = process.env.REACT_APP_SOCKET_URL || 'http://localhost:8081/ws';

const WebRTCComponent = () => {
  const [localStream, setLocalStream] = useState(null);
  const [remoteStream, setRemoteStream] = useState(null);
  const [peerConnection, setPeerConnection] = useState(null);
  const [stompClient, setStompClient] = useState(null);
  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);

  useEffect(() => {
    const socket = new SockJS(SOCKET_URL);
    const stomp = Client(socket);
    stomp.connect({}, onConnected, onError);
    setStompClient(stomp);

    return () => {
      if (localStream) {
        localStream.getTracks().forEach(track => track.stop());
      }
      if (peerConnection) {
        peerConnection.close();
      }
      if (stomp.connected) stomp.disconnect();
    };
  }, []);

  const onConnected = () => {
    console.log("Connected to WebSocket");
    stompClient.subscribe('/user/queue/webrtc', onWebRTCMessage);
  };

  const onError = (err) => {
    console.error("WebSocket Error:", err);
  };

  const onWebRTCMessage = (payload) => {
    const message = JSON.parse(payload.body);
    switch (message.type) {
      case 'OFFER':
        handleOffer(message);
        break;
      case 'ANSWER':
        handleAnswer(message);
        break;
      case 'ICE_CANDIDATE':
        handleIceCandidate(message);
        break;
      default:
        console.log('Unknown message type:', message.type);
    }
  };

  const createPeerConnection = () => {
    const configuration = {
      iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        {
          urls: 'turn:your-turn-server.com:3478',
          username: 'your-username',
          credential: 'your-password'
        }
      ]
    };

    const pc = new RTCPeerConnection(configuration);

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        sendMessage({
          type: 'ICE_CANDIDATE',
          candidate: event.candidate
        });
      }
    };

    pc.ontrack = (event) => {
      setRemoteStream(event.streams[0]);
    };

    return pc;
  };

  const startCall = async () => {
    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    setLocalStream(stream);
    localVideoRef.current.srcObject = stream;

    const pc = createPeerConnection();
    setPeerConnection(pc);

    stream.getTracks().forEach(track => pc.addTrack(track, stream));

    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);

    sendMessage({
      type: 'OFFER',
      sdp: pc.localDescription
    });
  };

  const handleOffer = async (message) => {
    const pc = createPeerConnection();
    setPeerConnection(pc);

    await pc.setRemoteDescription(new RTCSessionDescription(message.sdp));

    const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    setLocalStream(stream);
    localVideoRef.current.srcObject = stream;

    stream.getTracks().forEach(track => pc.addTrack(track, stream));

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    sendMessage({
      type: 'ANSWER',
      sdp: pc.localDescription
    });
  };

  const handleAnswer = async (message) => {
    await peerConnection.setRemoteDescription(new RTCSessionDescription(message.sdp));
  };

  const handleIceCandidate = async (message) => {
    if (peerConnection) {
      await peerConnection.addIceCandidate(new RTCIceCandidate(message.candidate));
    }
  };

  const sendMessage = (message) => {
    stompClient.send("/app/webrtc.message", {}, JSON.stringify(message));
  };

  return (
    <div>
      <video ref={localVideoRef} autoPlay muted playsInline />
      <video ref={remoteVideoRef} autoPlay playsInline />
      <button onClick={startCall}>Start Call</button>
    </div>
  );
};

export default WebRTCComponent;