/**
 * @file Loading.js
 * @description 
 * This component renders a full-screen loading page with a Lottie animation.
 * It includes:
 *  - A centered animated graphic using Lottie.
 *  - A loading message ("Loading, please wait...") displayed below the animation.
 * 
 * The animation plays automatically in a loop until the page or content is fully ready.
 * Layout and centering are handled via inline Flexbox styling.
 * 
 * Developer: Beiqi Dai, Zhaoyi Yang
 */


import { Player } from '@lottiefiles/react-lottie-player';
import animationData from './loading-animation.json';

function Loading() {
  return (
    <div style={{
      width: '100vw',
      height: '100vh',
      background: '#ffa680',
      display: 'flex',
      flexDirection: 'column',   
      justifyContent: 'center',
      alignItems: 'center',
    }}>
      {/* Play the Lottie animation */}
      <Player
        autoplay
        loop
        src={animationData}
        style={{ height: '300px', width: '300px' }}
      />

      {/* 🔥 add words */}
      <h2 style={{
        marginTop: '20px',
        fontSize: '24px',
        color: '#fff'
      }}>
        Loading, please wait...
      </h2>
    </div>
  );
}

export default Loading;
