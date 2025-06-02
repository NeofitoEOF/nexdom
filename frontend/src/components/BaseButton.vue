<template>
  <button :type="type" :class="['btn', btnClass]">
    <slot />
  </button>
</template>

<script setup lang="ts">
interface Props {
  type?: 'button' | 'submit' | 'reset';
  
  btnClass?: string;
}

withDefaults(defineProps<Props>(), {
  type: 'button'
});
</script>
