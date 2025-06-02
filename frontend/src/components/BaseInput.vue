<template>
  <div class="mb-4">
    <label v-if="label" :for="id" class="block text-sm font-medium text-gray-700 mb-1">{{ label }}</label>
    <input
      v-if="!textarea"
      :id="id"
      :type="type"
      v-model="modelValueProxy"
      :placeholder="placeholder"
      :class="['input', inputClass, { 'border-danger-500': error }]"
      @input="(e: Event) => $emit('update:modelValue', (e.target as HTMLInputElement)?.value)"
    />
    <textarea
      v-else
      :id="id"
      v-model="modelValueProxy"
      :placeholder="placeholder"
      :class="['input', inputClass, { 'border-danger-500': error }]"
      @input="(e: Event) => $emit('update:modelValue', (e.target as HTMLTextAreaElement)?.value)"
      rows="3"
    />
    <p v-if="error" class="mt-1 text-sm text-danger-600">{{ error }}</p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue?: string | number;
  
  label?: string;
  
  error?: string;
  
  id?: string;
  
  type?: string;
  
  placeholder?: string;
  
  textarea?: boolean;
  
  inputClass?: string;
}

withDefaults(defineProps<Props>(), {
  type: 'text',
  textarea: false
});

const modelValueProxy = defineModel<string | number>('modelValue');
</script>
