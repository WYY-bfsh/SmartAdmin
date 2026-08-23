import { defineStore } from 'pinia';
import { MUSIC_PLAY_MODE_ENUM } from '/@/constants/business/media/music-const';
import { musicApi } from '/@/api/business/media/music-api';
import { smartSentry } from '/@/lib/smart-sentry';

export const useMusicPlayerStore = defineStore({
  id: 'musicPlayerStore',
  state: () => ({
    playlist: [],
    currentIndex: 0,
    playing: false,
    currentTime: 0,
    duration: 0,
    volume: 0.7,
    mode: MUSIC_PLAY_MODE_ENUM.SEQUENCE.value,
    showLyric: false,
    likedIds: [],
    ready: false,
  }),
  getters: {
    currentSong(state) {
      return state.playlist[state.currentIndex] || null;
    },
    isLiked() {
      return this.likedIds.includes(this.currentSong?.songId);
    },
  },
  actions: {
    async bootstrap() {
      if (this.ready) {
        return;
      }
      try {
        const [favRes, songRes] = await Promise.all([musicApi.favorite(), musicApi.querySongs()]);
        this.likedIds = (favRes.data || []).map((s) => s.songId);
        if (!this.playlist.length && songRes.data?.length) {
          this.playlist = songRes.data;
          this.currentIndex = 0;
          this.playing = false;
        }
        this.ready = true;
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
    playList(songs, index = 0) {
      if (!songs || !songs.length) {
        return;
      }
      this.playlist = songs;
      this.currentIndex = index;
      this.playing = true;
      this.currentTime = 0;
      this.reportCurrent();
    },
    playSong(song, list) {
      if (!song) {
        return;
      }
      const source = list && list.length ? list : this.playlist;
      const idx = source.findIndex((s) => s.songId === song.songId);
      this.playlist = source.length ? source : [song];
      this.currentIndex = idx >= 0 ? idx : 0;
      if (idx < 0) {
        this.playlist = [song, ...source];
        this.currentIndex = 0;
      }
      this.playing = true;
      this.reportCurrent();
    },
    togglePlay() {
      if (!this.currentSong) {
        return;
      }
      this.playing = !this.playing;
    },
    next() {
      if (!this.playlist.length) {
        return;
      }
      if (this.mode === MUSIC_PLAY_MODE_ENUM.RANDOM.value) {
        this.currentIndex = Math.floor(Math.random() * this.playlist.length);
      } else {
        this.currentIndex = (this.currentIndex + 1) % this.playlist.length;
      }
      this.playing = true;
      this.currentTime = 0;
      this.reportCurrent();
    },
    prev() {
      if (!this.playlist.length) {
        return;
      }
      this.currentIndex = (this.currentIndex - 1 + this.playlist.length) % this.playlist.length;
      this.playing = true;
      this.currentTime = 0;
      this.reportCurrent();
    },
    cycleMode() {
      const modes = [MUSIC_PLAY_MODE_ENUM.SEQUENCE.value, MUSIC_PLAY_MODE_ENUM.LOOP.value, MUSIC_PLAY_MODE_ENUM.RANDOM.value];
      const i = modes.indexOf(this.mode);
      this.mode = modes[(i + 1) % modes.length];
    },
    async toggleLike(songId) {
      const id = songId || this.currentSong?.songId;
      if (!id) {
        return;
      }
      try {
        const res = await musicApi.toggleLike(id);
        if (res.data) {
          if (!this.likedIds.includes(id)) {
            this.likedIds = [...this.likedIds, id];
          }
        } else {
          this.likedIds = this.likedIds.filter((e) => e !== id);
        }
      } catch (e) {
        smartSentry.captureError(e);
      }
    },
    reportCurrent() {
      const id = this.currentSong?.songId;
      if (!id) {
        return;
      }
      musicApi.reportPlay(id).catch((e) => smartSentry.captureError(e));
    },
  },
});
