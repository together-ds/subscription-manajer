<template>
  <v-container class="fill-height">
    <v-responsive class="d-flex align-center text-center fill-height">
      <v-row class="d-flex align-center justify-center">
        <v-col cols="auto">
          <v-btn
            href="https://next.vuetifyjs.com/components/all/"
            min-width="164"
            rel="noopener noreferrer"
            target="_blank"
            variant="text"
          >
            <v-icon icon="mdi-view-dashboard" size="large" start />

            Components
          </v-btn>
        </v-col>

        <v-col cols="auto">
          <v-btn
            color="primary"
            href="https://next.vuetifyjs.com/introduction/why-vuetify/#feature-guides"
            min-width="228"
            rel="noopener noreferrer"
            size="x-large"
            target="_blank"
            variant="flat"
          >
            <v-icon icon="mdi-speedometer" size="large" start />

            Get Started
          </v-btn>
        </v-col>

        <v-col cols="auto">
          <v-btn
            href="https://community.vuetifyjs.com/"
            min-width="164"
            rel="noopener noreferrer"
            target="_blank"
            variant="text"
          >
            <v-icon icon="mdi-account-group" size="large" start />

            Community
          </v-btn>
        </v-col>
      </v-row>
      <v-row v-row class="d-flex align-center justify-center">
        <v-col cols="auto">
          <v-btn @click="decrement">-</v-btn>
        </v-col>

        <v-col cols="auto">
          {{ count }}
        </v-col>

        <v-col cols="auto">
          <v-btn @click="increment">+</v-btn>
        </v-col>
        <v-col cols="auto">
          <v-btn @click="createRandomBooks">create</v-btn>
        </v-col>
      </v-row>
      <v-row v-row class="d-flex align-center justify-center">
        <v-table>
          <thead>
            <tr>
              <th class="text-left">Id</th>
              <th class="text-left">Title</th>
              <th class="text-left">Author</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in books" :key="item.name">
              <td>{{ item.id }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.author }}</td>
            </tr>
          </tbody>
        </v-table>
      </v-row>
    </v-responsive>
  </v-container>
</template>


<script>
import axios from "axios";

export default {
  // data() 返回的属性将会成为响应式的状态
  // 并且暴露在 `this` 上
  data() {
    return {
      count: 0,
      books: [],
    };
  },

  // methods 是一些用来更改状态与触发更新的函数
  // 它们可以在模板中作为事件监听器绑定
  methods: {
    increment() {
      // log count and ++
      console.log(this.count);
      this.count++;
      console.log(this.count);
    },
    decrement() {
      // log count and --
      console.log(this.count);
      this.count--;
      console.log(this.count);
    },
    createRandomBooks() {
      this.books = [];
      for (var i = 0; i < this.count; i++) {
        //随机书名和作者，出版时间
        this.books.push({
          id: i + 1,
          title: this.randomName(6),
          author: this.randomName(10),
          published: this.randomDate(),
        });
      }
    },
    randomDate() {// generate a random date
      return new Date(Date.now() + Math.random() * 1000 * 60 * 60 * 24);
    },
    randomName(length){
      return Math.random().toString(36).substr(2, length);
    },
    
  },

  // 生命周期钩子会在组件生命周期的各个不同阶段被调用
  // 例如这个函数就会在组件挂载完成后被调用
  async mounted() {
    console.log(`The initial count is ${this.count}.`);
    let resp = await axios.get("/subscribe/get-template");
    this.axios = resp;
  },
};
</script>
