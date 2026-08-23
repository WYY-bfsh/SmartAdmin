<template>
  <a-table size="small" :dataSource="songs" :columns="columns" :pagination="false" rowKey="songId" :customRow="onRow">
    <template #bodyCell="{ record, column, index }">
      <template v-if="column.dataIndex === 'index'">
        <PlayCircleOutlined v-if="currentId === record.songId" style="color: #ec4141" />
        <span v-else>{{ index + 1 }}</span>
      </template>
      <template v-else-if="column.dataIndex === 'name'">
        <div class="song-name">
          <img :src="record.coverUrl" alt="" />
          <div>
            <div>{{ record.name }}</div>
            <div class="sub">{{ record.artist }}</div>
          </div>
        </div>
      </template>
      <template v-else-if="column.dataIndex === 'action'">
        <a-space>
          <a-button type="link" @click.stop="play(record)">播放</a-button>
          <a-button type="link" @click.stop="store.toggleLike(record.songId)">
            {{ store.likedIds.includes(record.songId) ? '已喜欢' : '喜欢' }}
          </a-button>
        </a-space>
      </template>
    </template>
  </a-table>
</template>

<script setup>
  import { computed } from 'vue';
  import { PlayCircleOutlined } from '@ant-design/icons-vue';
  import { useMusicPlayerStore } from '/@/store/modules/business/music-player';

  const props = defineProps({
    songs: { type: Array, default: () => [] },
  });

  const store = useMusicPlayerStore();
  const currentId = computed(() => store.currentSong?.songId);
  const columns = [
    { title: '#', dataIndex: 'index', width: 56 },
    { title: '歌曲', dataIndex: 'name' },
    { title: '专辑', dataIndex: 'album', width: 160 },
    { title: '时长', dataIndex: 'duration', width: 80 },
    { title: '操作', dataIndex: 'action', width: 160 },
  ];

  function play(song) {
    store.playSong(song, props.songs);
  }

  function onRow(record) {
    return {
      onDblclick: () => play(record),
    };
  }
</script>

<style scoped>
  .song-name {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .song-name img {
    width: 40px;
    height: 40px;
    border-radius: 4px;
    object-fit: cover;
  }
  .sub {
    color: #999;
    font-size: 12px;
  }
</style>
