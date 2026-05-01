<template>
  <NoteManagement initial-status="draft" />
</template>

<script>
import NoteManagement from './NoteManagement.vue'

export default {
  name: 'DraftManagement',
  components: {
    NoteManagement
  }
}
</script>
